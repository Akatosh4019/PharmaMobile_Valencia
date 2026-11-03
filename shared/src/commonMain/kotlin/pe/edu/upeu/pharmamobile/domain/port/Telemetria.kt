package pe.edu.upeu.pharmamobile.domain.port

interface Telemetria {
    fun registrarError(error: Throwable, contexto: Map<String, String> = emptyMap())
    fun registrarEvento(nombre: String, datos: Map<String, String> = emptyMap())
}

object TelemetriaSilenciosa : Telemetria {
    override fun registrarError(error: Throwable, contexto: Map<String, String>) = Unit
    override fun registrarEvento(nombre: String, datos: Map<String, String>) = Unit
}
