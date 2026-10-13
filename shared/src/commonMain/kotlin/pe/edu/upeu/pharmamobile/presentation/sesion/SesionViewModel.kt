package pe.edu.upeu.pharmamobile.presentation.sesion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobile.data.local.EstadoSesion
import pe.edu.upeu.pharmamobile.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobile.domain.repository.AuthRepository
import pe.edu.upeu.pharmamobile.domain.usecase.CerrarSesionUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.IniciarSesionUseCase

data class FormularioLogin(
    val usuario: String = "",
    val clave: String = "",
    val cargando: Boolean = false,
    val error: String? = null,
)

class SesionViewModel(
    private val iniciarSesion: IniciarSesionUseCase,
    private val cerrarSesion: CerrarSesionUseCase,
    private val repositorio: AuthRepository,
    private val estado: EstadoSesion,
) : ViewModel() {
    private val _formulario = MutableStateFlow(FormularioLogin())
    val formulario = _formulario.asStateFlow()
    val sesion = estado.sesion
    val inicializando = estado.inicializando
    val aviso = estado.aviso

    init {
        viewModelScope.launch { repositorio.sesionGuardada() }
    }

    fun cambiarUsuario(valor: String) {
        _formulario.value = _formulario.value.copy(usuario = valor, error = null)
    }

    fun cambiarClave(valor: String) {
        _formulario.value = _formulario.value.copy(clave = valor, error = null)
    }

    fun ingresar() {
        val actual = _formulario.value
        if (actual.cargando) return
        if (actual.usuario.isBlank() || actual.clave.isBlank()) {
            _formulario.value = actual.copy(error = "Escribe tu usuario y contraseña")
            return
        }
        _formulario.value = actual.copy(cargando = true, error = null)
        viewModelScope.launch {
            try {
                iniciarSesion(actual.usuario, actual.clave)
                _formulario.value = FormularioLogin()
            } catch (error: CancellationException) {
                throw error
            } catch (error: ErrorApiException) {
                _formulario.value = _formulario.value.copy(cargando = false, error = error.message)
            } catch (_: Exception) {
                _formulario.value = _formulario.value.copy(cargando = false, error = "No se pudo iniciar sesión")
            }
        }
    }

    fun salir() {
        viewModelScope.launch {
            cerrarSesion()
            _formulario.value = FormularioLogin()
        }
    }
}
