package pe.edu.upeu.pharmamobile.domain.service

import pe.edu.upeu.pharmamobile.domain.model.DetallePedido

interface DetallePedidoService {

    fun listar(): List<DetallePedido>

    fun buscarPorId(id: Long): DetallePedido?

    fun agregar(detalle: DetallePedido)

    fun actualizar(detalle: DetallePedido): Boolean

    fun eliminar(id: Long): Boolean

    fun listarPorPedido(pedidoId: Long): List<DetallePedido>
}
