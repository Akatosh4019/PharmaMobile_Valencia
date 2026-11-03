package pe.edu.upeu.pharmamobile.domain.model

data class ProductoFallido(
    val id: Long,
    val nombre: String,
    val intentos: Int,
    val ultimoError: String?,
)
