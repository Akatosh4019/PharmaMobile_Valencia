package pe.edu.upeu.pharmamobile.presentation.producto

import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.model.Categoria

sealed interface ProductoFase {
    data object Cargando : ProductoFase
    data object SinProductos : ProductoFase
    data class ConProductos(val productos: List<Producto>) : ProductoFase
    data class Error(val mensaje: String) : ProductoFase
}

sealed interface ProductoOperacion {
    data object Inactiva : ProductoOperacion
    data class EnCurso(val tipo: Tipo) : ProductoOperacion
    data class Fallida(val mensaje: String) : ProductoOperacion

    enum class Tipo { CREAR, ACTUALIZAR, DESACTIVAR }
}

enum class ProductoFiltro {
    ACTIVOS,
    INACTIVOS,
    BAJO_STOCK,
}

data class ProductoFormularioUiState(
    val nombre: String = "",
    val precio: String = "",
    val stock: String = "",
    val categoriaId: Long? = null,
    val activo: Boolean = true,
    val editandoId: Long? = null,
    val nombreError: String? = null,
    val precioError: String? = null,
    val stockError: String? = null,
    val categoriaError: String? = null,
    val intentoGuardar: Boolean = false,
)

data class ProductoUiState(
    val fase: ProductoFase = ProductoFase.Cargando,
    val productos: List<Producto> = emptyList(),
    val productosUi: List<ProductoUi> = emptyList(),
    val formulario: ProductoFormularioUiState = ProductoFormularioUiState(),
    val categorias: List<Categoria> = emptyList(),
    val operacion: ProductoOperacion = ProductoOperacion.Inactiva,
    val productoPendienteEliminar: Producto? = null,
    val mensajeError: String? = null,
    val avisoRed: String? = null,
    val mensajeResultado: String? = null,
    val filtro: ProductoFiltro = ProductoFiltro.ACTIVOS,
) {
    val operando: Boolean
        get() = operacion is ProductoOperacion.EnCurso

    val productosFiltrados: List<Producto>
        get() = productos.filter { producto ->
            when (filtro) {
                ProductoFiltro.ACTIVOS -> producto.activo
                ProductoFiltro.INACTIVOS -> !producto.activo
                ProductoFiltro.BAJO_STOCK -> producto.requiereReposicion()
            }
        }

    val productosUiFiltrados: List<ProductoUi>
        get() = productosUi.filter { item ->
            when (filtro) {
                ProductoFiltro.ACTIVOS -> item.producto.activo
                ProductoFiltro.INACTIVOS -> !item.producto.activo
                ProductoFiltro.BAJO_STOCK -> item.producto.requiereReposicion()
            }
        }
}
