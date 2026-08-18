package pe.edu.upeu.pharmamobile.domain.model

data class DetallePedido(
    val id: Long,
    val pedido: Pedido,
    val producto: Producto,
    val cantidad: Int,
    val precioUnitario: Double,
){
    init {
        require(cantidad > 0) {
            "La cantidad debe ser mayor que cero"
        }

        require(precioUnitario >= 0) {
            "El precio unitario no puede ser negativo"
        }
    }

    val subtotal: Double
        get() = cantidad * precioUnitario
}
