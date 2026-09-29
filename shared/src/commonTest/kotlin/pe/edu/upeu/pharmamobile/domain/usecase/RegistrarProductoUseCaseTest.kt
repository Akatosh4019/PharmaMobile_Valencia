package pe.edu.upeu.pharmamobile.domain.usecase

import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.model.Categoria
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RegistrarProductoUseCaseTest {
    private val repository = ProductoRepositoryFalso()
    private val useCase = RegistrarProductoUseCase(repository)

    @Test
    fun datosCorrectosRegistranProducto() = runTest {
        val resultado = useCase("Paracetamol 500 mg", "8.50", "100", 1L)

        assertTrue(resultado.isSuccess)
        assertEquals(1L, resultado.getOrThrow().id)
        assertEquals(1, repository.listar().size)
    }

    @Test
    fun nombreVacioGeneraError() {
        val errores = useCase.validar("", "8.50", "100", 1L)

        assertEquals("El nombre es obligatorio", errores.nombre)
    }

    @Test
    fun precioConTextoGeneraError() {
        val errores = useCase.validar("Ibuprofeno", "abc", "100", 1L)

        assertEquals("El precio debe ser numérico", errores.precio)
    }

    @Test
    fun precioCeroGeneraError() {
        val errores = useCase.validar("Ibuprofeno", "0", "100", 1L)

        assertEquals("El precio debe ser al menos 0.01", errores.precio)
    }

    @Test
    fun stockNegativoGeneraError() {
        val errores = useCase.validar("Ibuprofeno", "12", "-5", 1L)

        assertEquals("El stock no puede ser negativo", errores.stock)
    }

    @Test
    fun stockConTextoGeneraError() {
        val errores = useCase.validar("Ibuprofeno", "12", "abc", 1L)

        assertEquals("El stock debe ser un número entero", errores.stock)
    }

    @Test
    fun stockCeroEsValido() {
        val errores = useCase.validar("Loratadina", "10", "0", 1L)

        assertNull(errores.nombre)
        assertNull(errores.precio)
        assertNull(errores.stock)
    }

    @Test
    fun categoriaEsObligatoria() {
        assertEquals("Selecciona una categoría", useCase.validar("Loratadina", "10", "0").categoria)
    }
}

private class ProductoRepositoryFalso : ProductoRepository {
    private val productos = mutableListOf<Producto>()

    override suspend fun registrar(producto: Producto): Producto =
        producto.copy(id = (productos.size + 1).toLong()).also(productos::add)

    override suspend fun listar(): List<Producto> = productos.toList()

    override suspend fun listarCategorias(): List<Categoria> = listOf(Categoria(1L, "Medicamentos"))

    override suspend fun actualizar(producto: Producto): Producto = producto

    override suspend fun eliminar(id: Long) = Unit
}
