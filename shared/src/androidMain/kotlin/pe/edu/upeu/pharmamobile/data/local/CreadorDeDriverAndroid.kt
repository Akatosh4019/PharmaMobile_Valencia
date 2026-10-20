package pe.edu.upeu.pharmamobile.data.local

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

class CreadorDeDriverAndroid(private val contexto: Context) : CreadorDeDriver {
    override fun crear(): SqlDriver = AndroidSqliteDriver(
        schema = PharmaDatabase.Schema,
        context = contexto,
        name = "pharmamobile.db",
    )
}
