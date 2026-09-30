package pe.edu.upeu.pharmamobile.data.remote

import android.util.Log
import io.ktor.client.plugins.logging.Logger

actual fun loggerHttp(): Logger = object : Logger {
    override fun log(message: String) {
        Log.i("PharmaMobilHTTP", message)
    }
}
