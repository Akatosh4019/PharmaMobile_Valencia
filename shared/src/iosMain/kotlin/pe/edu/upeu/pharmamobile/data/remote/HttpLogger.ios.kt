package pe.edu.upeu.pharmamobile.data.remote

import io.ktor.client.plugins.logging.Logger

actual fun loggerHttp(): Logger = Logger.SIMPLE
