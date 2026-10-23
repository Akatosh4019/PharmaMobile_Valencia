package pe.edu.upeu.pharmamobile.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import pe.edu.upeu.pharmamobile.data.local.PharmaDatabase
import pe.edu.upeu.pharmamobile.data.local.ProductoLocal
import pe.edu.upeu.pharmamobile.data.remote.ProductoApi
import pe.edu.upeu.pharmamobile.domain.model.Producto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Cada test abre su propia SQLite en memoria; MockEngine representa PharmaSoft. */
internal expect fun crearBaseEnMemoria(): PharmaDatabase

class ProductoRepositorioOfflineTest {
    @Test
    fun conRedLaSincronizacionActualizaLaBaseYElFlow() = runTest {
        val servidor = ServidorSimulado().apply { productos += Producto(7, "Paracetamol", 15.5, 20) }
        try {
            val repo = servidor.repositorio(crearBaseEnMemoria())
            repo.sincronizar()
            assertEquals(listOf("Paracetamol"), repo.observar().first().map { it.nombre })
            assertTrue(repo.ultimaSincronizacionMillis() != null)
        } finally { servidor.cerrar() }
    }

    @Test
    fun sinRedElRegistroSiguePendienteYLoEmiteElFlow() = runTest {
        val servidor = ServidorSimulado().apply { conectado = false }
        try {
            val base = crearBaseEnMemoria()
            val repo = servidor.repositorio(base)
            val creado = repo.registrar(productoNuevo())
            assertTrue(creado.id < 0)
            assertEquals(creado, repo.observar().first().single())
            assertEquals(creado.id, ProductoLocal(base).pendientes().single().id)
        } finally { servidor.cerrar() }
    }

    @Test
    fun alReconectarSeEnviaLoPendienteYRecibeIdReal() = runTest {
        val servidor = ServidorSimulado().apply { conectado = false }
        try {
            val base = crearBaseEnMemoria()
            val repo = servidor.repositorio(base)
            repo.registrar(productoNuevo())
            servidor.conectado = true
            repo.sincronizar()
            assertEquals(listOf("POST", "GET"), servidor.operaciones)
            assertTrue(ProductoLocal(base).pendientes().isEmpty())
            assertTrue(repo.listar().single().id > 0)
        } finally { servidor.cerrar() }
    }

    @Test
    fun bajarCatalogoNoBorraLoCreadoSinRed() = runTest {
        val servidor = ServidorSimulado().apply { conectado = false }
        try {
            val base = crearBaseEnMemoria()
            val repo = servidor.repositorio(base)
            val pendiente = repo.registrar(productoNuevo())
            ProductoLocal(base).reemplazarSincronizados(listOf(Producto(8, "Paracetamol", 15.5, 20)))
            assertEquals(setOf(pendiente.id, 8L), repo.observar().first().map { it.id }.toSet())
            assertEquals(pendiente.id, ProductoLocal(base).pendientes().single().id)
        } finally { servidor.cerrar() }
    }

    @Test
    fun idTemporalNegativoNoColisionaConElDelServidor() = runTest {
        val servidor = ServidorSimulado().apply { conectado = false }
        try {
            val base = crearBaseEnMemoria()
            val repo = servidor.repositorio(base)
            val local = repo.registrar(productoNuevo())
            ProductoLocal(base).reemplazarSincronizados(listOf(Producto(1, "Paracetamol", 15.5, 20)))
            assertTrue(local.id < 0)
            assertEquals(2, repo.listar().size)
            assertEquals(2, repo.listar().map { it.id }.distinct().size)
        } finally { servidor.cerrar() }
    }

    @Test
    fun sincronizarDosVecesNoDuplicaFilas() = runTest {
        val servidor = ServidorSimulado().apply { productos += Producto(7, "Paracetamol", 15.5, 20) }
        try {
            val repo = servidor.repositorio(crearBaseEnMemoria())
            repo.sincronizar()
            repo.sincronizar()
            assertEquals(1, repo.listar().size)
            assertEquals(1, repo.listar().map { it.id }.distinct().size)
        } finally { servidor.cerrar() }
    }

    private fun productoNuevo() = Producto(0, "Ibuprofeno", 4.5, 20, categoriaId = 1)
}

private class ServidorSimulado {
    var conectado = true
    val productos = mutableListOf<Producto>()
    val operaciones = mutableListOf<String>()
    private val cabeceras = headersOf(HttpHeaders.ContentType, "application/json")
    private val cliente = HttpClient(MockEngine { solicitud ->
        if (!conectado) throw IOException("Sin red")
        when (solicitud.method) {
            HttpMethod.Post -> {
                operaciones += "POST"
                val creado = Producto((productos.maxOfOrNull { it.id } ?: 0) + 1, "Ibuprofeno", 4.5, 20, categoriaId = 1)
                productos += creado
                respond(creado.json(), headers = cabeceras)
            }
            else -> {
                operaciones += "GET"
                respond("""{"contenido":[${productos.joinToString(",") { it.json() }}],"pagina":0,"tamanio":20,"totalElementos":${productos.size},"totalPaginas":1,"ultima":true}""", headers = cabeceras)
            }
        }
    }) {
        expectSuccess = true
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        defaultRequest { url("http://localhost/api/v1/"); contentType(ContentType.Application.Json) }
    }

    fun repositorio(base: PharmaDatabase) = ProductoRepositorioOffline(ProductoRepositorioRemoto(ProductoApi(cliente)), base)
    fun cerrar() = cliente.close()

    private fun Producto.json(): String =
        """{"id":$id,"nombre":"$nombre","precio":$precio,"stock":$stock,"estado":$activo,"categoriaId":${categoriaId ?: "null"}}"""
}
