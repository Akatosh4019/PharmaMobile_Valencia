package pe.edu.upeu.pharmamobile.domain.serviceImpl

import pe.edu.upeu.pharmamobile.domain.model.Pedido
import pe.edu.upeu.pharmamobile.domain.service.PedidoService

class PedidoServiceImpl : PedidoService {

    private val pedidos = mutableListOf<Pedido>()

    override fun listar(): List<Pedido> = pedidos.toList()

    override fun buscarPorId(id: Long): Pedido? =
        pedidos.find { it.id == id }

    override fun agregar(pedido: Pedido) {
        pedidos.add(pedido)
    }

    override fun actualizar(pedido: Pedido): Boolean {
        val indice = pedidos.indexOfFirst { it.id == pedido.id }
        if (indice == -1) return false
        pedidos[indice] = pedido
        return true
    }

    override fun eliminar(id: Long): Boolean =
        pedidos.removeAll { it.id == id }

    override fun listarPorCliente(clienteId: Long): List<Pedido> =
        pedidos.filter { it.cliente.id == clienteId }
}
