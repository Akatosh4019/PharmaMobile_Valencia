package pe.edu.upeu.pharmamobile.data.local

import app.cash.sqldelight.db.SqlDriver

/** Solo la apertura de SQLite depende de la plataforma. */
interface CreadorDeDriver {
    fun crear(): SqlDriver
}
