package pe.edu.upeu.pharmamobile.domain.model

data class Producto(
    val id: Long,
    val nombre: String,
    val precio: Double,
    val stock: Int,
    val activo: Boolean = true,
) {
    fun requiereReposicion(): Boolean = stock <= STOCK_MINIMO

    companion object {
        const val STOCK_MINIMO = 5
    }
}
