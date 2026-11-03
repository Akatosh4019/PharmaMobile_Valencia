package pe.edu.upeu.pharmamobile.data.mapper

import kotlinx.coroutines.CancellationException
import pe.edu.upeu.pharmamobile.domain.error.ErrorApi
import pe.edu.upeu.pharmamobile.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobile.domain.model.ResultadoEnvio

/** Traduce los errores de transporte y HTTP a resultados entendibles por el dominio. */
internal fun Throwable.aResultadoEnvio(): ResultadoEnvio {
    if (this is CancellationException) throw this
    val error = (this as? ErrorApiException)?.error
    val motivo = message ?: "No se pudo completar el envío"
    return when (error) {
        ErrorApi.SinConexion, ErrorApi.TiempoAgotado, ErrorApi.Servidor -> ResultadoEnvio.Transitorio(motivo)
        is ErrorApi.Validacion, ErrorApi.NoEncontrado, is ErrorApi.Conflicto,
        ErrorApi.SinPermiso, ErrorApi.NoAutenticado, ErrorApi.CredencialesInvalidas,
        ErrorApi.RespuestaInvalida, null -> ResultadoEnvio.Permanente(motivo)
    }
}
