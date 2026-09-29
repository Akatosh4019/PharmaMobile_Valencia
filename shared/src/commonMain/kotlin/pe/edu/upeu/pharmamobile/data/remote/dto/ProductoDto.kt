package pe.edu.upeu.pharmamobile.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductoDto(
    val id: Long,
    val nombre: String,
    val precio: Double,
    val stock: Int,
    val estado: Boolean = true,
    val categoriaId: Long? = null,
    val categoriaNombre: String? = null,
)

@Serializable
data class PaginaProductosDto(
    val contenido: List<ProductoDto>,
    val pagina: Int,
    val tamanio: Int,
    val totalElementos: Long,
    val totalPaginas: Int,
    val ultima: Boolean,
)

