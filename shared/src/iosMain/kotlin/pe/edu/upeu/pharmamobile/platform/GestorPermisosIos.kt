package pe.edu.upeu.pharmamobile.platform

import kotlinx.coroutines.suspendCancellableCoroutine
import pe.edu.upeu.pharmamobile.domain.permiso.EstadoPermiso
import pe.edu.upeu.pharmamobile.domain.permiso.GestorPermisos
import pe.edu.upeu.pharmamobile.domain.permiso.Permiso
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusDenied
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVAuthorizationStatusRestricted
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import kotlin.coroutines.resume

class GestorPermisosIos : GestorPermisos {
    override suspend fun estado(permiso: Permiso): EstadoPermiso = when (
        AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
    ) {
        AVAuthorizationStatusAuthorized -> EstadoPermiso.Concedido
        AVAuthorizationStatusNotDetermined -> EstadoPermiso.Denegado
        AVAuthorizationStatusDenied, AVAuthorizationStatusRestricted -> EstadoPermiso.DenegadoParaSiempre
        else -> EstadoPermiso.Denegado
    }

    override suspend fun solicitar(permiso: Permiso): EstadoPermiso {
        if (estado(permiso) != EstadoPermiso.Denegado) return estado(permiso)
        val concedido = suspendCancellableCoroutine { continuation ->
            AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { resultado ->
                if (continuation.isActive) continuation.resume(resultado)
            }
        }
        return if (concedido) EstadoPermiso.Concedido else estado(permiso)
    }

    override fun abrirAjustes() {
        NSURL.URLWithString(UIApplicationOpenSettingsURLString)?.let { url ->
            UIApplication.sharedApplication.openURL(url)
        }
    }
}
