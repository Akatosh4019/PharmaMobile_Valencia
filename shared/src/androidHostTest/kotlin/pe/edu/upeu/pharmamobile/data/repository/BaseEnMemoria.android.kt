package pe.edu.upeu.pharmamobile.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import pe.edu.upeu.pharmamobile.data.local.PharmaDatabase

internal actual fun crearBaseEnMemoria(): PharmaDatabase {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    PharmaDatabase.Schema.create(driver)
    return PharmaDatabase(driver)
}
