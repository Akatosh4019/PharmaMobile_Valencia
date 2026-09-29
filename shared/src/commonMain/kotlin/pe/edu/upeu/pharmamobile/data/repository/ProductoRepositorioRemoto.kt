package pe.edu.upeu.pharmamobile.data.repository

import pe.edu.upeu.pharmamobile.data.mapper.aDominio
import pe.edu.upeu.pharmamobile.data.remote.ProductoApi
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository

class ProductoRepositorioRemoto(
    private val api: ProductoApi,
    private val respaldoLocal: ProductoRepositorioEnMemoria,
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
        // El POST remoto corresponde a la sesión 8; por ahora el alta sigue en memoria.
        return respaldoLocal.registrar(producto)
    }
}
