package pe.edu.upeu.pharmamobile.domain.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProductoTest {
    @Test
    fun productoConStockBajoRequiereReposicion() {
        val producto = Producto(1L, "Paracetamol", 8.50, 5)

        assertTrue(producto.requiereReposicion())
    }

    @Test
    fun productoConStockSuficienteNoRequiereReposicion() {
        val producto = Producto(1L, "Paracetamol", 8.50, Producto.STOCK_MINIMO + 1)

        assertFalse(producto.requiereReposicion())
    }
}
