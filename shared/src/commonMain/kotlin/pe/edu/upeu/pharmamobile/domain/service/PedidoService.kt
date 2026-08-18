package pe.edu.upeu.pharmamobile.domain.service

import pe.edu.upeu.pharmamobile.domain.model.Pedido

interface PedidoService {

    fun listar(): List<Pedido>

    fun buscarPorId(id: Long): Pedido?

    fun agregar(pedido: Pedido)

    fun actualizar(pedido: Pedido): Boolean

    fun eliminar(id: Long): Boolean

    fun listarPorCliente(clienteId: Long): List<Pedido>
}
