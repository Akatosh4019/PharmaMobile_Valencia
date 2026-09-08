package pe.edu.upeu.pharmamobile.presentation.producto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobile.domain.usecase.ProductoInvalidoException
import pe.edu.upeu.pharmamobile.domain.usecase.RegistrarProductoUseCase

class ProductoViewModel(
    private val registrarProducto: RegistrarProductoUseCase,
    private val repository: ProductoRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProductoUiState())
    val uiState = _uiState.asStateFlow()

    init {
        cargarProductos()
    }

    fun cambiarNombre(nombre: String) = actualizarFormulario(nombre = nombre)

    fun cambiarPrecio(precio: String) = actualizarFormulario(precio = precio)

    fun cambiarStock(stock: String) = actualizarFormulario(stock = stock)

    fun cambiarFiltro(filtro: ProductoFiltro) {
        _uiState.update { it.copy(filtro = filtro) }
    }

    fun registrar() {
        val formulario = _uiState.value.formulario
        _uiState.update {
            it.copy(
                fase = ProductoFase.Cargando,
                formulario = formulario.copy(intentoRegistrar = true),
                mensajeResultado = null,
            )
        }

        viewModelScope.launch {
            registrarProducto(
                nombre = formulario.nombre,
                precio = formulario.precio,
                stock = formulario.stock,
            ).onSuccess { producto ->
                _uiState.update {
                    it.copy(
                        formulario = ProductoFormularioUiState(),
                        mensajeResultado = "Producto ${producto.nombre} registrado correctamente",
                        filtro = if (producto.requiereReposicion()) {
                            ProductoFiltro.BAJO_STOCK
                        } else {
                            ProductoFiltro.ACTIVOS
                        },
                    )
                }
                cargarProductos()
            }.onFailure { error ->
                if (error is ProductoInvalidoException) {
                    _uiState.update {
                        it.copy(
                            fase = faseParaListaActual(),
                            formulario = formulario.copy(
                                nombreError = error.errores.nombre,
                                precioError = error.errores.precio,
                                stockError = error.errores.stock,
                                intentoRegistrar = true,
                            ),
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(fase = ProductoFase.Error(error.message ?: "No se pudo registrar el producto"))
                    }
                }
            }
        }
    }

    fun cargarProductos() {
        _uiState.update { it.copy(fase = ProductoFase.Cargando) }
        viewModelScope.launch {
            runCatching { repository.listar() }
                .onSuccess { productos ->
                    _uiState.update {
                        it.copy(
                            productos = productos,
                            fase = if (productos.isEmpty()) {
                                ProductoFase.SinProductos
                            } else {
                                ProductoFase.ConProductos(productos)
                            }
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(fase = ProductoFase.Error(error.message ?: "No se pudieron cargar los productos"))
                    }
                }
        }
    }

    private fun actualizarFormulario(
        nombre: String = _uiState.value.formulario.nombre,
        precio: String = _uiState.value.formulario.precio,
        stock: String = _uiState.value.formulario.stock,
    ) {
        val actual = _uiState.value.formulario
        val errores = if (actual.intentoRegistrar) {
            registrarProducto.validar(nombre, precio, stock)
        } else {
            null
        }

        _uiState.update {
            it.copy(
                formulario = actual.copy(
                    nombre = nombre,
                    precio = precio,
                    stock = stock,
                    nombreError = errores?.nombre,
                    precioError = errores?.precio,
                    stockError = errores?.stock,
                ),
                mensajeResultado = null,
            )
        }
    }

    private fun faseParaListaActual(): ProductoFase {
        val productos = _uiState.value.productos
        return if (productos.isEmpty()) {
            ProductoFase.SinProductos
        } else {
            ProductoFase.ConProductos(productos)
        }
    }
}
