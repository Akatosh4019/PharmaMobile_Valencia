package pe.edu.upeu.pharmamobile.demo

import pe.edu.upeu.pharmamobile.domain.model.Cliente

fun probarCliente(){
    val cliente = Cliente(
        id = 1L,
        nombre = "farmacia Nueva Vida",
        correo = "ventaas@central.pe",
        telefono = null
    )
    print(cliente.obtenertelefono())
}