package pe.edu.upeu.pharmamobile.presentation.producto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobile.domain.usecase.RegistrarProductoUseCase

class ProductoViewModel(
    private val registrarProducto: RegistrarProductoUseCase,
    private val repository: ProductoRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProductoUiState())
    val uiState = _uiState.asStateFlow()
    private var pantallaMostrada = false

    init {
        cargarCategorias()
        cargarProductos()
    }

    fun alMostrarPantalla() {
        if (pantallaMostrada) cargarProductos() else pantallaMostrada = true
    }

    fun cambiarNombre(nombre: String) = actualizarFormulario(nombre = nombre)
    fun cambiarPrecio(precio: String) = actualizarFormulario(precio = precio)
    fun cambiarStock(stock: String) = actualizarFormulario(stock = stock)
    fun cambiarCategoria(id: Long) = actualizarFormulario(categoriaId = id)
    fun cambiarActivo(activo: Boolean) = actualizarFormulario(activo = activo)

    fun cambiarFiltro(filtro: ProductoFiltro) {
        _uiState.update { it.copy(filtro = filtro) }
    }

    fun editar(producto: Producto) {
        _uiState.update {
            it.copy(
                formulario = ProductoFormularioUiState(
                    nombre = producto.nombre,
                    precio = producto.precio.toString(),
                    stock = producto.stock.toString(),
                    categoriaId = producto.categoriaId,
                    activo = producto.activo,
                    editandoId = producto.id,
                ),
                mensajeResultado = null,
                mensajeError = null,
            )
        }
    }

    fun cancelarEdicion() {
        _uiState.update { it.copy(formulario = ProductoFormularioUiState(), mensajeError = null) }
    }

    fun registrar() = guardar()

    fun guardar() {
        if (_uiState.value.operando) return
        val formulario = _uiState.value.formulario
        val errores = registrarProducto.validar(
            formulario.nombre, formulario.precio, formulario.stock, formulario.categoriaId,
        )
        if (errores.tieneErrores) {
            _uiState.update {
                it.copy(formulario = formulario.copy(
                    nombreError = errores.nombre,
                    precioError = errores.precio,
                    stockError = errores.stock,
                    categoriaError = errores.categoria,
                    intentoGuardar = true,
                ))
            }
            return
        }

        _uiState.update { it.copy(operando = true, mensajeError = null, mensajeResultado = null) }
        viewModelScope.launch {
            runCatching {
                val categoriaId = requireNotNull(formulario.categoriaId)
                if (formulario.editandoId == null) {
                    registrarProducto(
                        formulario.nombre, formulario.precio, formulario.stock, categoriaId,
                    ).getOrThrow()
                } else {
                    repository.actualizar(Producto(
                        id = formulario.editandoId,
                        nombre = formulario.nombre.trim(),
                        precio = formulario.precio.toDouble(),
                        stock = formulario.stock.toInt(),
                        activo = formulario.activo,
                        categoriaId = categoriaId,
                    ))
                }
            }.onSuccess { producto ->
                _uiState.update {
                    it.copy(
                        formulario = ProductoFormularioUiState(),
                        operando = false,
                        mensajeResultado = if (formulario.editandoId == null) {
                            "Producto ${producto.nombre} creado en el servidor"
                        } else {
                            "Producto ${producto.nombre} actualizado"
                        },
                        filtro = if (!producto.activo) ProductoFiltro.INACTIVOS else ProductoFiltro.ACTIVOS,
                    )
                }
                cargarProductos()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(operando = false, mensajeError = error.message ?: "No se pudo guardar el producto")
                }
            }
        }
    }

    fun solicitarEliminar(producto: Producto) {
        _uiState.update { it.copy(productoPendienteEliminar = producto) }
    }

    fun cancelarEliminacion() {
        _uiState.update { it.copy(productoPendienteEliminar = null) }
    }

    fun confirmarEliminar() {
        val producto = _uiState.value.productoPendienteEliminar ?: return
        if (_uiState.value.operando) return
        _uiState.update { it.copy(productoPendienteEliminar = null, operando = true, mensajeError = null) }
        viewModelScope.launch {
            runCatching { repository.eliminar(producto.id) }
                .onSuccess {
                    _uiState.update {
                        it.copy(operando = false, mensajeResultado = "${producto.nombre} desactivado", filtro = ProductoFiltro.INACTIVOS)
                    }
                    cargarProductos()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(operando = false, mensajeError = error.message ?: "No se pudo desactivar el producto")
                    }
                }
        }
    }

    fun cargarCategorias() {
        viewModelScope.launch {
            runCatching { repository.listarCategorias() }
                .onSuccess { categorias -> _uiState.update { it.copy(categorias = categorias) } }
                .onFailure { error ->
                    _uiState.update { it.copy(mensajeError = error.message ?: "No se pudieron cargar las categorías") }
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
                            fase = if (productos.isEmpty()) ProductoFase.SinProductos else ProductoFase.ConProductos(productos),
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
        categoriaId: Long? = _uiState.value.formulario.categoriaId,
        activo: Boolean = _uiState.value.formulario.activo,
    ) {
        val actual = _uiState.value.formulario
        val errores = if (actual.intentoGuardar) {
            registrarProducto.validar(nombre, precio, stock, categoriaId)
        } else null
        _uiState.update {
            it.copy(
                formulario = actual.copy(
                    nombre = nombre, precio = precio, stock = stock,
                    categoriaId = categoriaId, activo = activo,
                    nombreError = errores?.nombre,
                    precioError = errores?.precio,
                    stockError = errores?.stock,
                    categoriaError = errores?.categoria,
                ),
                mensajeError = null,
            )
        }
    }
}
