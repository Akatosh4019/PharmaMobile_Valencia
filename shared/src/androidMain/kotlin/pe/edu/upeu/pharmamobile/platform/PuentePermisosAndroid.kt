package pe.edu.upeu.pharmamobile.platform

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** Adapta el resultado asíncrono de Activity Result a una suspensión del código común. */
class PuentePermisosAndroid {
    private var actividad: ComponentActivity? = null
    private var lanzador: ActivityResultLauncher<String>? = null
    private var respuestaPendiente: CancellableContinuation<Boolean>? = null

    fun conectar(actividad: ComponentActivity) {
        this.actividad = actividad
        lanzador = actividad.registerForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
            respuestaPendiente?.let { continuation ->
                if (continuation.isActive) continuation.resume(concedido)
            }
            respuestaPendiente = null
        }
    }

    fun puedeMostrarExplicacion(nombrePermiso: String): Boolean =
        actividad?.let { ActivityCompat.shouldShowRequestPermissionRationale(it, nombrePermiso) } ?: false

    suspend fun solicitar(nombrePermiso: String): Boolean = suspendCancellableCoroutine { continuation ->
        val actual = lanzador
        if (actual == null) {
            continuation.resume(false)
            return@suspendCancellableCoroutine
        }
        respuestaPendiente = continuation
        continuation.invokeOnCancellation { if (respuestaPendiente === continuation) respuestaPendiente = null }
        actual.launch(nombrePermiso)
    }
}
