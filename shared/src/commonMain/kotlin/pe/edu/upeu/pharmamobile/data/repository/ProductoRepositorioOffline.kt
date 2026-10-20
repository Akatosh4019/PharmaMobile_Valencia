package pe.edu.upeu.pharmamobile.data.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.pharmamobile.data.local.PharmaDatabase
import pe.edu.upeu.pharmamobile.data.local.ProductoLocal
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
    private val local = ProductoLocal(base)

    override fun observar(): Flow<List<Producto>> = local.observar()

    override suspend fun listar(): List<Producto> = local.listar()

    override suspend fun registrar(producto: Producto): Producto {
        val pendiente = local.guardarPendiente(producto)
        return try {
            remoto.registrar(pendiente.copy(id = 0)).also {
                local.reemplazarPendiente(pendiente.id, it)
            }
        } catch (error: Exception) {
            if (error is CancellationException) throw error
            if (!error.esSinConexion()) {
                local.borrar(pendiente.id)
                throw error
            }
            pendiente
        }
    }

    override suspend fun obtener(id: Long): Producto =
        local.obtener(id) ?: remoto.obtener(id)

    override suspend fun listarCategorias(): List<Categoria> = try {
        remoto.listarCategorias().also(local::guardarCategorias)
    } catch (error: Exception) {
        if (error is CancellationException) throw error
        if (!error.esSinConexion()) throw error
        local.categorias()
    }

    override suspend fun actualizar(producto: Producto): Producto {
        if (producto.id < 0) {
            local.guardar(producto, pendiente = true)
            return producto
        }
        return remoto.actualizar(producto).also { local.guardar(it, pendiente = false) }
    }

    override suspend fun eliminar(id: Long) {
        if (id < 0) {
            local.obtener(id)?.let {
                local.guardar(it.copy(activo = false), pendiente = true)
            }
            return
        }
        remoto.eliminar(id)
        local.obtener(id)?.let {
            local.guardar(it.copy(activo = false), pendiente = false)
        }
    }

    override suspend fun sincronizar() {
        // Primero conservar y subir lo creado sin red. Solo después renovar la caché.
        local.pendientes().forEach { pendiente ->
            val creado = remoto.registrar(pendiente.copy(id = 0))
            local.reemplazarPendiente(pendiente.id, creado)
        }
        val recibidos = remoto.listar()
        local.reemplazarSincronizados(recibidos)
    }

    private fun Throwable.esSinConexion(): Boolean =
        (this as? ErrorApiException)?.error in setOf(ErrorApi.SinConexion, ErrorApi.TiempoAgotado)
}
