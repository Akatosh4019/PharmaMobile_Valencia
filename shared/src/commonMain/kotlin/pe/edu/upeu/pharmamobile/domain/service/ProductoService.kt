package pe.edu.upeu.pharmamobile.domain.service

import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.model.ResultadoProductos

interface ProductoService {

    fun listar(): List<Producto>

    fun buscarPorId(id: Long): Producto?

    fun agregar(producto: Producto)

    fun actualizar(producto: Producto): Boolean

    fun eliminar(id: Long): Boolean

    fun listarConStock(): List<Producto>

    suspend fun obtenerProductos(): List<Producto>

    fun observarEstados(): Flow<String>

    fun observarProductos(): Flow<List<Producto>>

    fun cargarProductos(): Flow<ResultadoProductos>
}
