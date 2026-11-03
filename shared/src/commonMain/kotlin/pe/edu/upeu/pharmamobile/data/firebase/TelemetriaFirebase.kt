package pe.edu.upeu.pharmamobile.data.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.crashlytics.crashlytics
import pe.edu.upeu.pharmamobile.domain.port.Telemetria

/** Adaptador externo: solo contexto técnico permitido llega a Crashlytics. */
class TelemetriaFirebase : Telemetria {
    private val clavesPermitidas = setOf("pantalla", "operacion", "tipo_error")

    override fun registrarError(error: Throwable, contexto: Map<String, String>) {
        runCatching {
            // No se envía el mensaje original: puede contener campos introducidos por el usuario.
            val errorSeguro = Exception(error::class.simpleName ?: "Fallo tecnico")
            Firebase.crashlytics.recordException(errorSeguro, contextoTecnico(contexto))
        }
    }

    override fun registrarEvento(nombre: String, datos: Map<String, String>) {
        runCatching {
            val resumen = contextoTecnico(datos).entries.joinToString(" ") { (clave, valor) -> "$clave=$valor" }
            Firebase.crashlytics.log((nombre.take(80) + " " + resumen).take(300))
        }
    }

    private fun contextoTecnico(datos: Map<String, String>): Map<String, String> =
        datos.filterKeys { it in clavesPermitidas }
            .mapValues { (_, valor) -> valor.take(120) }
}
