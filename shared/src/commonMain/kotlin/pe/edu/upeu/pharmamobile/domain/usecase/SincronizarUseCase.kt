package pe.edu.upeu.pharmamobile.domain.usecase

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import pe.edu.upeu.pharmamobile.domain.model.ResultadoEnvio
import pe.edu.upeu.pharmamobile.domain.model.ResumenSincronizacion
import pe.edu.upeu.pharmamobile.domain.repository.ProductoColaLocalRepository
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRemotoSyncRepository
import kotlin.time.Clock

class SincronizarUseCase(
    private val local: ProductoColaLocalRepository,
    private val remoto: ProductoRemotoSyncRepository,
) {
    suspend operator fun invoke(): ResumenSincronizacion {
        var enviadas = 0
        var fallidas = 0

        local.pendientesParaEnviar(MAX_INTENTOS).forEach { fila ->
            // La espera ocurre antes del nuevo intento para limitar peticiones consecutivas.
            if (fila.intentos > 0) delay(espera(fila.intentos))
            when (val resultado = remoto.enviar(fila.producto)) {
                is ResultadoEnvio.Exito -> {
                    local.confirmarPendiente(fila.producto.id, resultado.producto)
                    enviadas++
                }
                is ResultadoEnvio.Permanente -> {
                    local.marcarFallida(fila.producto.id, MAX_INTENTOS, resultado.motivo)
                    fallidas++
                }
                is ResultadoEnvio.Transitorio -> {
                    val intentosActuales = local.sumarIntento(fila.producto.id, resultado.motivo)
                    if (intentosActuales >= MAX_INTENTOS) {
                        local.marcarFallida(fila.producto.id, MAX_INTENTOS, resultado.motivo)
                        fallidas++
                    }
                }
            }
        }

        // Subir primero evita que la conciliación borre registros que solo existen en el dispositivo.
        try {
            local.conciliar(remoto.listar())
            local.guardarUltimaSync(Clock.System.now().toEpochMilliseconds())
        } catch (error: CancellationException) {
            throw error
        }

        val estado = local.contarCola(MAX_INTENTOS)
        return ResumenSincronizacion(enviadas, maxOf(fallidas, estado.fallidas), estado.pendientes)
    }

    private fun espera(intentos: Int): Long = 1_000L shl (intentos - 1).coerceIn(0, 3)

    companion object {
        const val MAX_INTENTOS = 5
    }
}
