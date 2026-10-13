package pe.edu.upeu.pharmamobile.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import pe.edu.upeu.pharmamobile.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobile.data.local.EstadoSesion
import pe.edu.upeu.pharmamobile.data.remote.dto.LoginRequestDto
import pe.edu.upeu.pharmamobile.data.remote.dto.LoginResponseDto
import pe.edu.upeu.pharmamobile.data.remote.dto.UsuarioActualDto
import pe.edu.upeu.pharmamobile.data.remote.ejecutarLlamada
import pe.edu.upeu.pharmamobile.domain.error.ErrorApi
import pe.edu.upeu.pharmamobile.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobile.domain.model.Credenciales
import pe.edu.upeu.pharmamobile.domain.model.Sesion
import pe.edu.upeu.pharmamobile.domain.repository.AuthRepository

class AuthRepositorioRest(
    private val cliente: HttpClient,
    private val almacen: AlmacenSeguro,
    private val estado: EstadoSesion,
) : AuthRepository {
    override suspend fun iniciarSesion(credenciales: Credenciales): Sesion {
        val dto = try {
            ejecutarLlamada {
                cliente.post("auth/login") {
                    setBody(LoginRequestDto(credenciales.usuario, credenciales.clave))
                }.body<LoginResponseDto>()
            }
        } catch (error: ErrorApiException) {
            if (error.error == ErrorApi.NoAutenticado) {
                throw ErrorApiException(ErrorApi.CredencialesInvalidas)
            }
            throw error
        }
        almacen.guardarToken(dto.token)
        cliente.authProvider<BearerAuthProvider>()?.clearToken()
        return Sesion(dto.token, dto.username, dto.nombreCompleto, dto.permisos.toSet())
            .also(estado::activar)
    }

    override suspend fun sesionGuardada(): Sesion? {
        val token = almacen.leerToken()
        if (token == null) {
            estado.finalizarInicio()
            return null
        }
        return try {
            cliente.authProvider<BearerAuthProvider>()?.clearToken()
            val dto = ejecutarLlamada { cliente.get("auth/me").body<UsuarioActualDto>() }
            Sesion(token, dto.username, dto.empleadoNombre, dto.permisos.toSet())
                .also(estado::activar)
        } catch (error: Exception) {
            if (error is kotlinx.coroutines.CancellationException) throw error
            if (error is ErrorApiException && error.error == ErrorApi.NoAutenticado) {
                estado.limpiar("Tu sesión expiró. Inicia sesión nuevamente")
            } else {
                estado.finalizarInicio()
            }
            null
        }
    }

    override suspend fun cerrarSesion() {
        estado.limpiar()
        cliente.authProvider<BearerAuthProvider>()?.clearToken()
    }
}
