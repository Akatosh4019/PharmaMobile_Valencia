package pe.edu.upeu.pharmamobile.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.edu.upeu.pharmamobile.data.local.PharmaDatabase
import pe.edu.upeu.pharmamobile.domain.error.ErrorApi
import pe.edu.upeu.pharmamobile.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobile.domain.model.Categoria
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository

/** La interfaz siempre lee SQLite. La red solo actualiza su contenido. */
class ProductoRepositorioOffline(
    private val remoto: ProductoRepository,
    private val base: PharmaDatabase,
) : ProductoRepository {
    private val consultas = base.productoQueries

    override fun observar(): Flow<List<Producto>> = consultas.listarTodos(::aDominio)
        .asFlow().mapToList(Dispatchers.Default).map { it.toList() }

    override suspend fun listar(): List<Producto> = consultas.listarTodos(::aDominio).executeAsList()

    override suspend fun registrar(producto: Producto): Producto = try {
        remoto.registrar(producto).also { guardar(it, pendiente = false) }
    } catch (error: Exception) {
        if (error is CancellationException) throw error
        if (!error.esSinConexion()) throw error
        val local = producto.copy(id = consultas.idTemporalSiguiente().executeAsOne())
        guardar(local, pendiente = true)
        local
    }

    override suspend fun obtener(id: Long): Producto =
        consultas.buscarPorId(id, ::aDominio).executeAsOneOrNull() ?: remoto.obtener(id)

    override suspend fun listarCategorias(): List<Categoria> = try {
        remoto.listarCategorias().also { categorias ->
            base.transaction {
                categorias.forEach { consultas.guardarCategoria(it.id, it.nombre) }
            }
        }
    } catch (error: Exception) {
        if (error is CancellationException) throw error
        if (!error.esSinConexion()) throw error
        consultas.listarCategorias().executeAsList().map { Categoria(it.id, it.nombre) }
    }

    override suspend fun actualizar(producto: Producto): Producto {
        if (producto.id < 0) {
            guardar(producto, pendiente = true)
            return producto
        }
        return remoto.actualizar(producto).also { guardar(it, pendiente = false) }
    }

    override suspend fun eliminar(id: Long) {
        if (id < 0) {
            consultas.buscarPorId(id, ::aDominio).executeAsOneOrNull()?.let {
                guardar(it.copy(activo = false), pendiente = true)
            }
            return
        }
        remoto.eliminar(id)
        consultas.buscarPorId(id, ::aDominio).executeAsOneOrNull()?.let {
            guardar(it.copy(activo = false), pendiente = false)
        }
    }

    override suspend fun sincronizar() {
        // Primero conservar y subir lo creado sin red. Solo después renovar la caché.
        consultas.listarPendientes(::aDominio).executeAsList().forEach { pendiente ->
            val creado = remoto.registrar(pendiente.copy(id = 0))
            base.transaction {
                consultas.eliminarProducto(pendiente.id)
                guardar(creado, pendiente = false)
            }
        }
        val recibidos = remoto.listar()
        base.transaction {
            consultas.eliminarSincronizados()
            recibidos.forEach { guardar(it, pendiente = false) }
        }
    }

    private fun guardar(producto: Producto, pendiente: Boolean) {
        consultas.guardarProducto(
            producto.id, producto.nombre, producto.precio, producto.stock.toLong(),
            if (producto.activo) 1 else 0, producto.categoriaId,
            producto.categoriaNombre, if (pendiente) 1 else 0,
        )
    }

    private fun aDominio(
        id: Long, nombre: String, precio: Double, stock: Long, activo: Long,
        categoria_id: Long?, categoria_nombre: String?, pendiente: Long,
    ): Producto = Producto(
        id = id, nombre = nombre, precio = precio, stock = stock.toInt(),
        activo = activo != 0L, categoriaId = categoria_id,
        categoriaNombre = categoria_nombre,
    )

    private fun Throwable.esSinConexion(): Boolean =
        (this as? ErrorApiException)?.error in setOf(ErrorApi.SinConexion, ErrorApi.TiempoAgotado)
}
