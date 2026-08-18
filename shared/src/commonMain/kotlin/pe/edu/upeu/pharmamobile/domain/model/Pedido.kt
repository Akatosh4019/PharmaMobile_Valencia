package pe.edu.upeu.pharmamobile.domain.model

data class Pedido(
    val id: Long,
    val cliente: Cliente,
    val producto: Producto,
    val cantidad: Int,
    val fecha: String,
    val estado: String,
    val total: Double,
)
