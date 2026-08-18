package pe.edu.upeu.pharmamobile.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class ClienteTest {

    @Test
    fun probarCliente(){
        val cliente = Cliente(
            id = 1L,
            nombre = "farmacia Nueva Vida",
            correo = "ventaas@central.pe",
            telefono = null
        )
        val resultado = cliente.obtenertelefono()

        assertEquals(
            expected = "No hay telefono registrado",
            actual = resultado
        )
    }
}
