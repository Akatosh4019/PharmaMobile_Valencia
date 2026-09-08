package pe.edu.upeu.pharmamobile.data.repository

import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobile.domain.model.Producto
import kotlin.test.Test
import kotlin.test.assertEquals

class ProductoRepositorioEnMemoriaTest {
    @Test
    fun repositorioAsignaIdsConsecutivos() = runTest {
        val repository = ProductoRepositorioEnMemoria()

        val primero = repository.registrar(Producto(0L, "Paracetamol", 8.50, 10))
        val segundo = repository.registrar(Producto(0L, "Ibuprofeno", 12.00, 5))

        assertEquals(6L, primero.id)
        assertEquals(7L, segundo.id)
        assertEquals(listOf(primero, segundo), repository.listar().takeLast(2))
    }
}
