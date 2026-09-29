package pe.edu.upeu.pharmamobile.domain.repository

import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.model.Categoria

interface ProductoRepository {
    suspend fun registrar(producto: Producto): Producto
    suspend fun listar(): List<Producto>
    suspend fun listarCategorias(): List<Categoria>
    suspend fun actualizar(producto: Producto): Producto
    suspend fun eliminar(id: Long)
}
