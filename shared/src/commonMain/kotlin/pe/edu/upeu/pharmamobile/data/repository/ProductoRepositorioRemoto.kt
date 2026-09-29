package pe.edu.upeu.pharmamobile.data.repository

import pe.edu.upeu.pharmamobile.data.mapper.aDominio
import pe.edu.upeu.pharmamobile.data.remote.ProductoApi
import pe.edu.upeu.pharmamobile.data.remote.dto.ProductoRequestDto
import pe.edu.upeu.pharmamobile.domain.model.Categoria
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository

class ProductoRepositorioRemoto(
    private val api: ProductoApi,
) : ProductoRepository {
    override suspend fun listar(): List<Producto> {
        val productos = mutableListOf<Producto>()
        var pagina = 0
        do {
            val respuesta = api.obtenerProductos(pagina++)
            productos += respuesta.contenido.map { it.aDominio() }
        } while (!respuesta.ultima)
        return productos
    }

    override suspend fun registrar(producto: Producto): Producto {
        return api.crear(producto.aRequest()).aDominio()
    }

    override suspend fun listarCategorias(): List<Categoria> =
        api.obtenerCategorias().map { Categoria(it.id, it.nombre) }

    override suspend fun actualizar(producto: Producto): Producto =
        api.actualizar(producto.id, producto.aRequest()).aDominio()

    override suspend fun eliminar(id: Long) = api.eliminar(id)

    private fun Producto.aRequest(): ProductoRequestDto = ProductoRequestDto(
        nombre = nombre,
        precio = precio,
        stock = stock,
        estado = activo,
        categoriaId = requireNotNull(categoriaId) { "Selecciona una categoría" },
    )
}
