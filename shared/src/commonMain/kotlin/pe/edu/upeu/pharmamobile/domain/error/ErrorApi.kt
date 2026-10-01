package pe.edu.upeu.pharmamobile.domain.error

sealed interface ErrorApi {
    data class Validacion(val porCampo: Map<String, String>) : ErrorApi
    data object NoEncontrado : ErrorApi
    data class Conflicto(val mensaje: String) : ErrorApi
    data object Servidor : ErrorApi
    data object SinConexion : ErrorApi
    data object TiempoAgotado : ErrorApi
    data object RespuestaInvalida : ErrorApi
}

fun ErrorApi.mensajeUsuario(): String = when (this) {
    is ErrorApi.Validacion -> "Revisa los datos marcados en el formulario"
    ErrorApi.NoEncontrado -> "El producto ya no está disponible"
    is ErrorApi.Conflicto -> mensaje
    ErrorApi.Servidor -> "El servidor no pudo completar la operación. Inténtalo de nuevo"
    ErrorApi.SinConexion -> "Sin conexión con el servidor. Comprueba tu red e inténtalo de nuevo"
    ErrorApi.TiempoAgotado -> "La solicitud tardó demasiado. Inténtalo de nuevo"
    ErrorApi.RespuestaInvalida -> "La respuesta del servidor no tiene el formato esperado"
}

class ErrorApiException(val error: ErrorApi) : Exception(error.mensajeUsuario())
