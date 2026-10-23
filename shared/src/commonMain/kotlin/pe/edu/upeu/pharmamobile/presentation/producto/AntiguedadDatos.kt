package pe.edu.upeu.pharmamobile.presentation.producto

import kotlin.time.Clock

/** La hora se conserva en la base; este texto se calcula para mostrarla al usuario. */
fun textoAntiguedadDatos(ultimaSincronizacionMillis: Long?, ahoraMillis: Long = Clock.System.now().toEpochMilliseconds()): String {
    if (ultimaSincronizacionMillis == null) return "Nunca se ha sincronizado"
    val minutos = ((ahoraMillis - ultimaSincronizacionMillis).coerceAtLeast(0) / 60_000)
    return when {
        minutos < 1 -> "Actualizado hace menos de un minuto"
        minutos < 60 -> "Actualizado hace $minutos ${if (minutos == 1L) "minuto" else "minutos"}"
        minutos < 1_440 -> {
            val horas = minutos / 60
            "Actualizado hace $horas ${if (horas == 1L) "hora" else "horas"}"
        }
        else -> {
            val dias = minutos / 1_440
            "Actualizado hace $dias ${if (dias == 1L) "día" else "días"}"
        }
    }
}
