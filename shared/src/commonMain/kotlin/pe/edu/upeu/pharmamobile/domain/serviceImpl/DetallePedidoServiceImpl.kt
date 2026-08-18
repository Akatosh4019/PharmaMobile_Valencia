package pe.edu.upeu.pharmamobile.domain.serviceImpl

import pe.edu.upeu.pharmamobile.domain.model.DetallePedido
import pe.edu.upeu.pharmamobile.domain.service.DetallePedidoService

class DetallePedidoServiceImpl : DetallePedidoService {

    private val detalles = mutableListOf<DetallePedido>()

    override fun listar(): List<DetallePedido> = detalles.toList()

    override fun buscarPorId(id: Long): DetallePedido? =
        detalles.find { it.id == id }

    override fun agregar(detalle: DetallePedido) {
        detalles.add(detalle)
    }

    override fun actualizar(detalle: DetallePedido): Boolean {
        val indice = detalles.indexOfFirst { it.id == detalle.id }
        if (indice == -1) return false
        detalles[indice] = detalle
        return true
    }

    override fun eliminar(id: Long): Boolean =
        detalles.removeAll { it.id == id }

    override fun listarPorPedido(pedidoId: Long): List<DetallePedido> =
        detalles.filter { it.pedido.id == pedidoId }
}
