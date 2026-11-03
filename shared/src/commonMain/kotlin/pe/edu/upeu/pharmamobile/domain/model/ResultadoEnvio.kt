package pe.edu.upeu.pharmamobile.domain.model

sealed interface ResultadoEnvio {
    data class Exito(val producto: Producto) : ResultadoEnvio
    data class Transitorio(val motivo: String) : ResultadoEnvio
    data class Permanente(val motivo: String) : ResultadoEnvio
}

data class ResumenSincronizacion(
    val enviadas: Int = 0,
    val fallidas: Int = 0,
    val pendientes: Int = 0,
)
