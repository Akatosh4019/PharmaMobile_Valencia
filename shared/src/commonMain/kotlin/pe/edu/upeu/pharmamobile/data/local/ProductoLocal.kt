package pe.edu.upeu.pharmamobile.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.edu.upeu.pharmamobile.domain.model.Categoria
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.model.EstadoCola
import pe.edu.upeu.pharmamobile.domain.model.ProductoFallido
import pe.edu.upeu.pharmamobile.domain.model.ProductoPendienteEnvio
import pe.edu.upeu.pharmamobile.domain.repository.ProductoColaLocalRepository

/** La capa local traduce filas SQLDelight a modelos de dominio, sin conocer Ktor ni DTO. */
class ProductoLocal(private val base: PharmaDatabase) : ProductoColaLocalRepository {
    private val consultas = base.productoQueries

    fun observar(): Flow<List<Producto>> = consultas.listarTodos(::aDominio)
        .asFlow().mapToList(Dispatchers.Default).map { it.toList() }

    fun listar(): List<Producto> = consultas.listarTodos(::aDominio).executeAsList()

    fun obtener(id: Long): Producto? = consultas.buscarPorId(id, ::aDominio).executeAsOneOrNull()

    override fun pendientesParaEnviar(maxIntentos: Int): List<ProductoPendienteEnvio> =
        consultas.listarPendientes(maxIntentos.toLong()) { id, nombre, precio, stock, activo,
            categoriaId, categoriaNombre, _, intentos, ultimoError ->
            ProductoPendienteEnvio(
                producto = aDominio(id, nombre, precio, stock, activo, categoriaId, categoriaNombre, 1, intentos, ultimoError),
                intentos = intentos.toInt(),
                ultimoError = ultimoError,
            )
        }.executeAsList()

    /** Mantiene la consulta simple previa para pantallas y pruebas existentes. */
    fun pendientes(): List<Producto> = pendientesParaEnviar(maxIntentos = 5).map { it.producto }

    override fun observarPendientes(maxIntentos: Int): Flow<Int> =
        consultas.contarPendientes(maxIntentos.toLong()).asFlow()
            .mapToOne(Dispatchers.Default).map(Long::toInt)

    fun observarFallidas(maxIntentos: Int): Flow<Int> =
        consultas.contarFallidas(maxIntentos.toLong()).asFlow()
            .mapToOne(Dispatchers.Default).map(Long::toInt)

    override fun observarFallidasDetalle(maxIntentos: Int): Flow<List<ProductoFallido>> =
        consultas.listarFallidas(maxIntentos.toLong())
            .asFlow().mapToList(Dispatchers.Default).map { filas ->
                filas.map { fila ->
                    ProductoFallido(fila.id, fila.nombre, fila.intentos.toInt(), fila.ultimo_error)
                }
            }

    override fun contarCola(maxIntentos: Int): EstadoCola = EstadoCola(
        pendientes = consultas.contarPendientes(maxIntentos.toLong()).executeAsOne().toInt(),
        fallidas = consultas.contarFallidas(maxIntentos.toLong()).executeAsOne().toInt(),
    )

    override fun sumarIntento(id: Long, error: String): Int {
        consultas.sumarIntento(error, id)
        return intentos(id)
    }

    fun intentos(id: Long): Int = consultas.intentosPorId(id).executeAsOneOrNull()?.toInt() ?: 0

    override fun marcarFallida(id: Long, maxIntentos: Int, error: String) =
        consultas.marcarFallida(maxIntentos.toLong(), error, id)

    override fun confirmarPendiente(idTemporal: Long, producto: Producto) = reemplazarPendiente(idTemporal, producto)

    override fun conciliar(productos: List<Producto>) = reemplazarSincronizados(productos)

    override fun guardarUltimaSync(instanteMillis: Long) = guardarSincronizacion(instanteMillis)

    override fun ultimaSyncMillis(): Long? = ultimaSincronizacionMillis()

    fun categorias(): List<Categoria> = consultas.listarCategorias().executeAsList()
        .map { Categoria(it.id, it.nombre) }

    fun ultimaSincronizacionMillis(): Long? =
        consultas.ultimaSincronizacion().executeAsOneOrNull()

    fun guardarSincronizacion(instanteMillis: Long) {
        consultas.guardarSincronizacion(instanteMillis)
    }

    fun guardarCategorias(categorias: List<Categoria>) {
        base.transaction { categorias.forEach { consultas.guardarCategoria(it.id, it.nombre) } }
    }

    fun guardarPendiente(producto: Producto): Producto {
        val local = producto.copy(id = consultas.idTemporalSiguiente().executeAsOne())
        guardar(local, pendiente = true)
        return local
    }

    fun guardar(
        producto: Producto,
        pendiente: Boolean,
        intentos: Long = 0,
        ultimoError: String? = null,
    ) {
        consultas.guardarProducto(
            producto.id, producto.nombre, producto.precio, producto.stock.toLong(),
            if (producto.activo) 1 else 0, producto.categoriaId,
            producto.categoriaNombre, if (pendiente) 1 else 0, intentos, ultimoError,
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
        intentos: Long, ultimo_error: String?,
    ): Producto = Producto(
        id = id, nombre = nombre, precio = precio, stock = stock.toInt(),
        activo = activo != 0L, categoriaId = categoria_id,
        categoriaNombre = categoria_nombre,
    )
}
