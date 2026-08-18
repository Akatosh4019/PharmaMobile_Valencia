package pe.edu.upeu.pharmamobile.domain.service

import pe.edu.upeu.pharmamobile.domain.model.Cliente

interface ClienteService {

    fun listar(): List<Cliente>

    fun buscarPorId(id: Long): Cliente?

    fun agregar(cliente: Cliente)

    fun actualizar(cliente: Cliente): Boolean

    fun eliminar(id: Long): Boolean
}

