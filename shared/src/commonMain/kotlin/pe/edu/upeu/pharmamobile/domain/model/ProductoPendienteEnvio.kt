package pe.edu.upeu.pharmamobile.domain.model

data class ProductoPendienteEnvio(
    val producto: Producto,
    val intentos: Int,
    val ultimoError: String?,
)
