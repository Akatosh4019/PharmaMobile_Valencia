package pe.edu.upeu.pharmamobile.data.local

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import pe.edu.upeu.pharmamobile.domain.model.Sesion

class EstadoSesion(private val almacen: AlmacenSeguro) {
    private val _sesion = MutableStateFlow<Sesion?>(null)
    val sesion = _sesion.asStateFlow()

    private val _inicializando = MutableStateFlow(true)
    val inicializando = _inicializando.asStateFlow()
    private val _aviso = MutableStateFlow<String?>(null)
    val aviso = _aviso.asStateFlow()

    fun activar(sesion: Sesion) {
        _sesion.value = sesion
        _inicializando.value = false
        _aviso.value = null
    }

    fun finalizarInicio() {
        _inicializando.value = false
    }

    fun limpiar(aviso: String? = null) {
        almacen.borrarToken()
        _sesion.value = null
        _inicializando.value = false
        _aviso.value = aviso
    }
}
