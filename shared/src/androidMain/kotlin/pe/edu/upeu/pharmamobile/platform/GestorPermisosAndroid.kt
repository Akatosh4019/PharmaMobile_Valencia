package pe.edu.upeu.pharmamobile.platform

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.content.ContextCompat
import pe.edu.upeu.pharmamobile.domain.permiso.EstadoPermiso
import pe.edu.upeu.pharmamobile.domain.permiso.GestorPermisos
import pe.edu.upeu.pharmamobile.domain.permiso.Permiso

class GestorPermisosAndroid(
    private val contexto: Context,
    private val puente: PuentePermisosAndroid,
) : GestorPermisos {
    private val preferencias = contexto.getSharedPreferences("permisos_nativos", Context.MODE_PRIVATE)

    override suspend fun estado(permiso: Permiso): EstadoPermiso {
        val nombre = permiso.nombreAndroid()
        if (ContextCompat.checkSelfPermission(contexto, nombre) == PackageManager.PERMISSION_GRANTED) {
            return EstadoPermiso.Concedido
        }
        val solicitudes = preferencias.getInt("solicitudes_$nombre", 0)
        val puedeExplicar = puente.puedeMostrarExplicacion(nombre)
        return when {
            solicitudes == 0 || puedeExplicar || solicitudes < 2 -> EstadoPermiso.Denegado
            else -> EstadoPermiso.DenegadoParaSiempre
        }
    }

    override suspend fun solicitar(permiso: Permiso): EstadoPermiso {
        val nombre = permiso.nombreAndroid()
        preferencias.edit().putInt("solicitudes_$nombre", preferencias.getInt("solicitudes_$nombre", 0) + 1).apply()
        puente.solicitar(nombre)
        return estado(permiso)
    }

    override fun abrirAjustes() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${contexto.packageName}"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        contexto.startActivity(intent)
    }

    private fun Permiso.nombreAndroid(): String = when (this) {
        Permiso.CAMARA -> Manifest.permission.CAMERA
    }
}
