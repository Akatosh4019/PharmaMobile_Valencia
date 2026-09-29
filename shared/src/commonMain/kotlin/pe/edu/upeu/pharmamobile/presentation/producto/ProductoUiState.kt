package pe.edu.upeu.pharmamobile.presentation.producto

import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.model.Categoria

sealed interface ProductoFase {
    data object Cargando : ProductoFase
    data object SinProductos : ProductoFase
    data class ConProductos(val productos: List<Producto>) : ProductoFase
    data class Error(val mensaje: String) : ProductoFase
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
    val formulario: ProductoFormularioUiState = ProductoFormularioUiState(),
    val categorias: List<Categoria> = emptyList(),
    val operando: Boolean = false,
    val productoPendienteEliminar: Producto? = null,
    val mensajeError: String? = null,
    val mensajeResultado: String? = null,
    val filtro: ProductoFiltro = ProductoFiltro.ACTIVOS,
) {
    val productosFiltrados: List<Producto>
        get() = productos.filter { producto ->
            when (filtro) {
                ProductoFiltro.ACTIVOS -> producto.activo
                ProductoFiltro.INACTIVOS -> !producto.activo
                ProductoFiltro.BAJO_STOCK -> producto.requiereReposicion()
            }
        }
}
