package pe.edu.upeu.pharmamobile.domain.repository

import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.model.Categoria
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface ProductoRepository {
    suspend fun registrar(producto: Producto): Producto
    suspend fun listar(): List<Producto>
    suspend fun obtener(id: Long): Producto
    suspend fun listarCategorias(): List<Categoria>
    suspend fun actualizar(producto: Producto): Producto
    suspend fun eliminar(id: Long)
    fun observar(): Flow<List<Producto>> = flow { emit(listar()) }
    suspend fun sincronizar() { listar() }
    fun ultimaSincronizacionMillis(): Long? = null
}
