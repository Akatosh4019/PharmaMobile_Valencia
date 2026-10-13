package pe.edu.upeu.pharmamobile.domain.usecase

import pe.edu.upeu.pharmamobile.domain.model.Credenciales
import pe.edu.upeu.pharmamobile.domain.repository.AuthRepository

class IniciarSesionUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(usuario: String, clave: String) =
        repository.iniciarSesion(Credenciales(usuario.trim(), clave))
}

class CerrarSesionUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke() = repository.cerrarSesion()
}
