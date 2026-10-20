package pe.edu.upeu.pharmamobile.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

class CreadorDeDriverIos : CreadorDeDriver {
    override fun crear(): SqlDriver = NativeSqliteDriver(
        schema = PharmaDatabase.Schema,
        name = "pharmamobile.db",
    )
}
