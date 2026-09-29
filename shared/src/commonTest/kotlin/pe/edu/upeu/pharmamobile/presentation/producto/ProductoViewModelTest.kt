package pe.edu.upeu.pharmamobile.presentation.producto

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.model.Categoria
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobile.domain.usecase.RegistrarProductoUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class ProductoViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun prepararDispatcher() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun restaurarDispatcher() {
        Dispatchers.resetMain()
    }

    @Test
    fun repositorioVacioProduceSinProductos() = runTest(dispatcher.scheduler) {
        val repository = ProductoRepositoryFalso(productosIniciales = emptyList())
        val viewModel = crearViewModel(repository)

        advanceUntilIdle()

        assertIs<ProductoFase.SinProductos>(viewModel.uiState.value.fase)
    }

    @Test
    fun repositorioConTresProductosProduceConProductos() = runTest(dispatcher.scheduler) {
        val productos = listOf(
            Producto(1L, "Paracetamol", 15.50, 100),
            Producto(2L, "Ibuprofeno", 18.90, 50),
            Producto(3L, "Amoxicilina", 25.00, 5),
        )
        val repository = ProductoRepositoryFalso(productosIniciales = productos)
        val viewModel = crearViewModel(repository)

        advanceUntilIdle()

        val fase = assertIs<ProductoFase.ConProductos>(viewModel.uiState.value.fase)
        assertEquals(productos, fase.productos)
    }

    @Test
    fun repositorioQueLanzaExcepcionProduceError() = runTest(dispatcher.scheduler) {
        val repository = ProductoRepositoryFalso(errorAlListar = "No se pudo conectar con el inventario")
        val viewModel = crearViewModel(repository)

        advanceUntilIdle()

        val fase = assertIs<ProductoFase.Error>(viewModel.uiState.value.fase)
        assertEquals("No se pudo conectar con el inventario", fase.mensaje)
    }

    @Test
    fun precioCeroMuestraErrorSinLlamarAlRepositorio() = runTest(dispatcher.scheduler) {
        val repository = ProductoRepositoryFalso(productosIniciales = emptyList())
        val viewModel = crearViewModel(repository)
        advanceUntilIdle()

        viewModel.cambiarNombre("Paracetamol")
        viewModel.cambiarPrecio("0")
        viewModel.cambiarStock("10")
        viewModel.cambiarCategoria(1L)
        viewModel.registrar()
        advanceUntilIdle()

        assertEquals("El precio debe ser al menos 0.01", viewModel.uiState.value.formulario.precioError)
        assertEquals(0, repository.llamadasARegistrar)
    }

    @Test
    fun alVolverAPantallaRecargaProductosDelRepositorio() = runTest(dispatcher.scheduler) {
        val repository = ProductoRepositoryFalso(productosIniciales = emptyList())
        val viewModel = crearViewModel(repository)
        advanceUntilIdle()

        viewModel.alMostrarPantalla()
        assertEquals(1, repository.llamadasAListar)

        repository.agregarProducto(Producto(10L, "Paracetamolado", 12.0, 23))
        viewModel.alMostrarPantalla()
        advanceUntilIdle()

        assertEquals(2, repository.llamadasAListar)
        val fase = assertIs<ProductoFase.ConProductos>(viewModel.uiState.value.fase)
        assertEquals("Paracetamolado", fase.productos.single().nombre)
    }

    @Test
    fun crearProductoLoGuardaYActualizaListado() = runTest(dispatcher.scheduler) {
        val repository = ProductoRepositoryFalso()
        val viewModel = crearViewModel(repository)
        advanceUntilIdle()

        viewModel.cambiarNombre("Vitamina C")
        viewModel.cambiarPrecio("12.50")
        viewModel.cambiarStock("20")
        viewModel.cambiarCategoria(1L)
        viewModel.guardar()
        advanceUntilIdle()

        assertEquals(1, repository.llamadasARegistrar)
        assertEquals("Vitamina C", viewModel.uiState.value.productos.single().nombre)
        assertEquals(1L, viewModel.uiState.value.productos.single().categoriaId)
    }

    @Test
    fun editarYReactivarProductoActualizaRepositorio() = runTest(dispatcher.scheduler) {
        val original = Producto(2L, "Loratadina", 10.0, 0, activo = false, categoriaId = 1L)
        val repository = ProductoRepositoryFalso(productosIniciales = listOf(original))
        val viewModel = crearViewModel(repository)
        advanceUntilIdle()

        viewModel.editar(original)
        viewModel.cambiarStock("10")
        viewModel.cambiarActivo(true)
        viewModel.guardar()
        advanceUntilIdle()

        assertEquals(1, repository.llamadasAActualizar)
        assertEquals(true, viewModel.uiState.value.productos.single().activo)
        assertEquals(10, viewModel.uiState.value.productos.single().stock)
    }

    @Test
    fun desactivarProductoLoConservaEnInactivos() = runTest(dispatcher.scheduler) {
        val original = Producto(3L, "Amoxicilina", 25.0, 5, categoriaId = 1L)
        val repository = ProductoRepositoryFalso(productosIniciales = listOf(original))
        val viewModel = crearViewModel(repository)
        advanceUntilIdle()

        viewModel.solicitarEliminar(original)
        viewModel.confirmarEliminar()
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.productos.size)
        assertEquals(false, viewModel.uiState.value.productos.single().activo)
        assertEquals(ProductoFiltro.INACTIVOS, viewModel.uiState.value.filtro)
    }

    private fun crearViewModel(repository: ProductoRepository): ProductoViewModel =
        ProductoViewModel(
            registrarProducto = RegistrarProductoUseCase(repository),
            repository = repository,
        )
}

private class ProductoRepositoryFalso(
    productosIniciales: List<Producto> = emptyList(),
    private val errorAlListar: String? = null,
) : ProductoRepository {
    private val productos = productosIniciales.toMutableList()
    var llamadasARegistrar: Int = 0
        private set
    var llamadasAListar: Int = 0
        private set
    var llamadasAActualizar: Int = 0
        private set

    fun agregarProducto(producto: Producto) {
        productos.add(producto)
    }

    override suspend fun registrar(producto: Producto): Producto {
        llamadasARegistrar++
        val registrado = producto.copy(id = (productos.maxOfOrNull { it.id } ?: 0L) + 1L)
        productos.add(registrado)
        return registrado
    }

    override suspend fun listar(): List<Producto> {
        llamadasAListar++
        errorAlListar?.let { error(it) }
        return productos.toList()
    }

    override suspend fun listarCategorias(): List<Categoria> = listOf(Categoria(1L, "Medicamentos"))

    override suspend fun actualizar(producto: Producto): Producto {
        llamadasAActualizar++
        val indice = productos.indexOfFirst { it.id == producto.id }
        productos[indice] = producto
        return producto
    }

    override suspend fun eliminar(id: Long) {
        val indice = productos.indexOfFirst { it.id == id }
        productos[indice] = productos[indice].copy(activo = false)
    }
}
