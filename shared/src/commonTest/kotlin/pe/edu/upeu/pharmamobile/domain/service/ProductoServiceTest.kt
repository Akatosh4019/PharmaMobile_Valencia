package pe.edu.upeu.pharmamobile.domain.service

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobile.domain.model.ResultadoProductos
import pe.edu.upeu.pharmamobile.domain.serviceImpl.ProductoServiceImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProductoServiceTest {

    private val servicio: ProductoService = ProductoServiceImpl()

    @Test
    fun observarSecuenciaDeEstados() = runTest {
        val estados = servicio.observarEstados().toList()

        estados.forEach { println("Estado: $it") }

        assertEquals(
            listOf("Iniciando", "Procesando", "Finalizado"),
            estados
        )
    }

    @Test
    fun observarCambiosDeProductos() = runTest {
        val emisiones = servicio.observarProductos().toList()

        emisiones.forEachIndexed { indice, productos ->
            println("Emisión ${indice + 1}: $productos")
        }

        assertTrue(emisiones.first().isEmpty())
        assertEquals(3, emisiones[1].size)
        assertEquals(90, emisiones.last().first().stock)
    }

    @Test
    fun cargarProductosConResultado() = runTest {
        val resultados = servicio.cargarProductos().toList()

        resultados.forEach { println("Resultado: $it") }

        assertTrue(resultados.first() is ResultadoProductos.Cargando)
        assertTrue(resultados.last() is ResultadoProductos.Exito)
    }
}
