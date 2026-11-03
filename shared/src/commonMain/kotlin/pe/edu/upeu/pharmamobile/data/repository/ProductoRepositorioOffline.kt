package pe.edu.upeu.pharmamobile.data.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.pharmamobile.data.mapper.aResultadoEnvio
import pe.edu.upeu.pharmamobile.data.local.PharmaDatabase
import pe.edu.upeu.pharmamobile.data.local.ProductoLocal
import pe.edu.upeu.pharmamobile.domain.model.Categoria
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.model.ResultadoEnvio
import pe.edu.upeu.pharmamobile.domain.model.ResumenSincronizacion
import pe.edu.upeu.pharmamobile.domain.error.ErrorApi
import pe.edu.upeu.pharmamobile.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobile.domain.usecase.SincronizarUseCase

/** La interfaz siempre lee SQLite. La red solo actualiza su contenido. */
class ProductoRepositorioOffline(
    private val remoto: ProductoRepository,
    private val base: PharmaDatabase,
    localCompartido: ProductoLocal? = null,
    sincronizarCompartido: SincronizarUseCase? = null,
) : ProductoRepository {
    private val local = localCompartido ?: ProductoLocal(base)
    private val sincronizador = sincronizarCompartido
        ?: SincronizarUseCase(local, ProductoRemotoSyncAdapter(remoto))
    private val maxIntentos = SincronizarUseCase.MAX_INTENTOS

    override fun observar(): Flow<List<Producto>> = local.observar()

    override suspend fun listar(): List<Producto> = local.listar()

    override fun ultimaSincronizacionMillis(): Long? = local.ultimaSincronizacionMillis()

    override fun observarPendientes() = local.observarPendientes(maxIntentos)

    override fun observarFallidasDetalle() = local.observarFallidasDetalle(maxIntentos)

    override suspend fun registrar(producto: Producto): Producto {
        val pendiente = local.guardarPendiente(producto)
        return try {
            remoto.registrar(pendiente.copy(id = 0)).also {
                local.reemplazarPendiente(pendiente.id, it)
            }
        } catch (error: Exception) {
            if (error is CancellationException) throw error
            when (val resultado = error.aResultadoEnvio()) {
                is ResultadoEnvio.Transitorio -> {
                    local.sumarIntento(pendiente.id, resultado.motivo)
                    if (local.intentos(pendiente.id) >= maxIntentos) {
                        local.marcarFallida(pendiente.id, maxIntentos, resultado.motivo)
                    }
                    pendiente
                }
                is ResultadoEnvio.Permanente -> {
                    // Una respuesta inválida no permite saber si el servidor alcanzó a guardar el POST.
                    if ((error as? ErrorApiException)?.error == ErrorApi.RespuestaInvalida) {
                        local.marcarFallida(pendiente.id, maxIntentos, resultado.motivo)
                        return pendiente
                    }
                    local.borrar(pendiente.id)
                    throw error
                }
                is ResultadoEnvio.Exito -> throw IllegalStateException("Resultado de envío inesperado")
            }
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
        sincronizador()
    }

    override suspend fun sincronizarResumen(): ResumenSincronizacion = sincronizador()
}
