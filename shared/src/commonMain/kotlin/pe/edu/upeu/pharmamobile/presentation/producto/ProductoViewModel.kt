package pe.edu.upeu.pharmamobile.presentation.producto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.error.ErrorApi
import pe.edu.upeu.pharmamobile.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobile.domain.error.mensajeUsuario
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobile.domain.platform.Compartidor
import pe.edu.upeu.pharmamobile.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.EliminarProductoUseCase

class ProductoViewModel(
    private val registrarProducto: RegistrarProductoUseCase,
    private val repository: ProductoRepository,
    private val actualizarProducto: ActualizarProductoUseCase,
    private val eliminarProducto: EliminarProductoUseCase,
    private val compartidor: Compartidor,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProductoUiState())
    val uiState = _uiState.asStateFlow()
    private var pantallaMostrada = false

    init {
        viewModelScope.launch {
            repository.observar().collect { productos ->
                _uiState.update {
                    it.copy(
                        productos = productos,
                        productosUi = productos.map { producto -> producto.toUi() },
                        fase = if (productos.isEmpty()) ProductoFase.SinProductos else ProductoFase.ConProductos(productos),
                    )
                }
            }
        }
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

    fun compartir(productoUi: ProductoUi) {
        try {
            compartidor.compartir(productoUi.comoTextoParaCompartir())
        } catch (error: Exception) {
            if (error is CancellationException) throw error
            _uiState.update { it.copy(mensajeError = "No se pudo abrir la opción de compartir") }
        }
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
                operacion = ProductoOperacion.Inactiva,
            )
        }
    }

    fun cancelarEdicion() {
        _uiState.update { it.copy(formulario = ProductoFormularioUiState(), mensajeError = null, operacion = ProductoOperacion.Inactiva) }
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

        val tipo = if (formulario.editandoId == null) ProductoOperacion.Tipo.CREAR else ProductoOperacion.Tipo.ACTUALIZAR
        _uiState.update { it.copy(operacion = ProductoOperacion.EnCurso(tipo), mensajeError = null, mensajeResultado = null) }
        viewModelScope.launch {
            runCatching {
                val categoriaId = requireNotNull(formulario.categoriaId)
                if (formulario.editandoId == null) {
                    registrarProducto(
                        formulario.nombre, formulario.precio, formulario.stock, categoriaId,
                    ).getOrThrow()
                } else {
                    actualizarProducto(Producto(
                        id = formulario.editandoId,
                        nombre = formulario.nombre.trim(),
                        precio = formulario.precio.toDouble(),
                        stock = formulario.stock.toInt(),
                        activo = formulario.activo,
                        categoriaId = categoriaId,
                    )).getOrThrow()
                }
            }.onSuccess { producto ->
                _uiState.update {
                    it.copy(
                        formulario = ProductoFormularioUiState(),
                        operacion = ProductoOperacion.Inactiva,
                        mensajeResultado = if (formulario.editandoId == null) {
                            if (producto.id < 0) "Producto ${producto.nombre} guardado; pendiente de sincronizar"
                            else "Producto ${producto.nombre} creado en el servidor"
                        } else {
                            "Producto ${producto.nombre} actualizado"
                        },
                        filtro = if (!producto.activo) ProductoFiltro.INACTIVOS else ProductoFiltro.ACTIVOS,
                    )
                }
            }.onFailure { error ->
                manejarFallo(error)
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
        _uiState.update { it.copy(productoPendienteEliminar = null, operacion = ProductoOperacion.EnCurso(ProductoOperacion.Tipo.DESACTIVAR), mensajeError = null) }
        viewModelScope.launch {
            eliminarProducto(producto.id)
                .onSuccess {
                    _uiState.update {
                        it.copy(operacion = ProductoOperacion.Inactiva, mensajeResultado = "${producto.nombre} desactivado", filtro = ProductoFiltro.INACTIVOS)
                    }
                }
                .onFailure { error ->
                    manejarFallo(error)
                }
        }
    }

    fun cargarCategorias() {
        viewModelScope.launch {
            runCatching { repository.listarCategorias() }
                .onSuccess { categorias -> _uiState.update { it.copy(categorias = categorias) } }
                .onFailure { error ->
                    if (error is CancellationException) throw error
                    _uiState.update { it.copy(mensajeError = mensajeDe(error)) }
                }
        }
    }

    fun cargarProductos() = recargarProductos()

    private fun recargarProductos() {
        if (_uiState.value.productos.isEmpty()) {
            _uiState.update { it.copy(fase = ProductoFase.Cargando) }
        }
        viewModelScope.launch {
            runCatching { repository.sincronizar() }
                .onSuccess { _uiState.update { it.copy(avisoRed = null) } }
                .onFailure { error ->
                    if (error is CancellationException) throw error
                    _uiState.update {
                        if (it.productos.isNotEmpty()) {
                            it.copy(avisoRed = "Sin conexión con el servidor. Comprueba tu red e inténtalo de nuevo")
                        } else {
                            it.copy(fase = ProductoFase.Error(mensajeDe(error)))
                        }
                    }
                }
        }
    }

    private fun manejarFallo(fallo: Throwable) {
        if (fallo is CancellationException) throw fallo
        val error = (fallo as? ErrorApiException)?.error
        if (error is ErrorApi.Validacion) {
            _uiState.update { actual ->
                actual.copy(
                    operacion = ProductoOperacion.Inactiva,
                    formulario = actual.formulario.copy(
                        nombreError = error.porCampo["nombre"],
                        precioError = error.porCampo["precio"],
                        stockError = error.porCampo["stock"],
                        categoriaError = error.porCampo["categoriaId"],
                        intentoGuardar = true,
                    ),
                    mensajeError = if (error.porCampo.isEmpty()) error.mensajeUsuario() else null,
                )
            }
        } else {
            _uiState.update {
                it.copy(operacion = ProductoOperacion.Fallida(mensajeDe(fallo)), mensajeError = null)
            }
        }
    }

    private fun mensajeDe(fallo: Throwable): String =
        (fallo as? ErrorApiException)?.error?.mensajeUsuario()
            ?: fallo.message ?: "No se pudo completar la operación"

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
                operacion = if (it.operacion is ProductoOperacion.Fallida) ProductoOperacion.Inactiva else it.operacion,
            )
        }
    }
}
