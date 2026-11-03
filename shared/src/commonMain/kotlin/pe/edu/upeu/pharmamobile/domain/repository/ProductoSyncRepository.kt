package pe.edu.upeu.pharmamobile.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.pharmamobile.domain.model.EstadoCola
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.model.ProductoFallido
import pe.edu.upeu.pharmamobile.domain.model.ProductoPendienteEnvio
import pe.edu.upeu.pharmamobile.domain.model.ResultadoEnvio

interface ProductoColaLocalRepository {
    fun observarPendientes(maxIntentos: Int): Flow<Int>
    fun observarFallidasDetalle(maxIntentos: Int): Flow<List<ProductoFallido>>
    fun pendientesParaEnviar(maxIntentos: Int): List<ProductoPendienteEnvio>
    fun sumarIntento(id: Long, error: String): Int
    fun marcarFallida(id: Long, maxIntentos: Int, error: String)
    fun confirmarPendiente(idTemporal: Long, producto: Producto)
    fun conciliar(productos: List<Producto>)
    fun guardarUltimaSync(instanteMillis: Long)
    fun ultimaSyncMillis(): Long?
    fun contarCola(maxIntentos: Int): EstadoCola
}

interface ProductoRemotoSyncRepository {
    suspend fun enviar(producto: Producto): ResultadoEnvio
    suspend fun listar(): List<Producto>
}
