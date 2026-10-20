package pe.edu.upeu.pharmamobile.data.repository

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import pe.edu.upeu.pharmamobile.data.local.PharmaDatabase

internal actual fun crearBaseEnMemoria(): PharmaDatabase = PharmaDatabase(
    NativeSqliteDriver(PharmaDatabase.Schema, ":memory:")
)
