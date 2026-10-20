package pe.edu.upeu.pharmamobile.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.edu.upeu.pharmamobile.domain.model.Categoria
import pe.edu.upeu.pharmamobile.domain.model.Producto

/** La capa local traduce filas SQLDelight a modelos de dominio, sin conocer Ktor ni DTO. */
class ProductoLocal(private val base: PharmaDatabase) {
    private val consultas = base.productoQueries

    fun observar(): Flow<List<Producto>> = consultas.listarTodos(::aDominio)
        .asFlow().mapToList(Dispatchers.Default).map { it.toList() }

    fun listar(): List<Producto> = consultas.listarTodos(::aDominio).executeAsList()

    fun obtener(id: Long): Producto? = consultas.buscarPorId(id, ::aDominio).executeAsOneOrNull()

    fun pendientes(): List<Producto> = consultas.listarPendientes(::aDominio).executeAsList()

    fun categorias(): List<Categoria> = consultas.listarCategorias().executeAsList()
        .map { Categoria(it.id, it.nombre) }

    fun guardarCategorias(categorias: List<Categoria>) {
        base.transaction { categorias.forEach { consultas.guardarCategoria(it.id, it.nombre) } }
    }

    fun guardarPendiente(producto: Producto): Producto {
        val local = producto.copy(id = consultas.idTemporalSiguiente().executeAsOne())
        guardar(local, pendiente = true)
        return local
    }

    fun guardar(producto: Producto, pendiente: Boolean) {
        consultas.guardarProducto(
            producto.id, producto.nombre, producto.precio, producto.stock.toLong(),
            if (producto.activo) 1 else 0, producto.categoriaId,
            producto.categoriaNombre, if (pendiente) 1 else 0,
        )
    }

    fun borrar(id: Long) = consultas.eliminarProducto(id)

    fun reemplazarPendiente(idTemporal: Long, productoServidor: Producto) {
        base.transaction {
            consultas.eliminarProducto(idTemporal)
            guardar(productoServidor, pendiente = false)
        }
    }

    fun reemplazarSincronizados(productos: List<Producto>) {
        base.transaction {
            consultas.eliminarSincronizados()
            productos.forEach { guardar(it, pendiente = false) }
        }
    }

    private fun aDominio(
        id: Long, nombre: String, precio: Double, stock: Long, activo: Long,
        categoria_id: Long?, categoria_nombre: String?, pendiente: Long,
    ): Producto = Producto(
        id = id, nombre = nombre, precio = precio, stock = stock.toInt(),
        activo = activo != 0L, categoriaId = categoria_id,
        categoriaNombre = categoria_nombre,
    )
}
