package pe.edu.upeu.pharmamobile.domain.serviceImpl

import pe.edu.upeu.pharmamobile.domain.model.Cliente
import pe.edu.upeu.pharmamobile.domain.service.ClienteService

class ClienteServiceImpl : ClienteService {
    private val clientes = mutableListOf<Cliente>()

    override fun listar(): List<Cliente> {
        return clientes.toList()
    }

    override fun buscarPorId(id: Long): Cliente? {
        return clientes.find { cliente ->
            cliente.id == id
        }
    }

    override fun agregar(cliente: Cliente) {
        clientes.add(cliente)
    }

    override fun actualizar(cliente: Cliente): Boolean {
        val indice = clientes.indexOfFirst { it.id == cliente.id }
        if (indice == -1) return false
        clientes[indice] = cliente
        return true
    }

    override fun eliminar(id: Long): Boolean {
        return clientes.removeAll { cliente ->
            cliente.id == id
        }
    }
}
