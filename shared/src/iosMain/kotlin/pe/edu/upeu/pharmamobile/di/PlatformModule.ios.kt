package pe.edu.upeu.pharmamobile.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.qualifier.named
import org.koin.core.module.Module
import org.koin.dsl.module
import pe.edu.upeu.pharmamobile.domain.platform.Compartidor
import pe.edu.upeu.pharmamobile.platform.CompartidorIos
import pe.edu.upeu.pharmamobile.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobile.data.local.AlmacenSeguroIos
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import pe.edu.upeu.pharmamobile.data.local.PharmaDatabase

actual val platformModule: Module = module {
    single<SqlDriver> { NativeSqliteDriver(PharmaDatabase.Schema, "pharmamobile.db") }
    single<HttpClientEngine> { Darwin.create() }
    single(named("apiBaseUrl")) { "http://localhost:8080/api/v1/" }
    single<Compartidor> { CompartidorIos() }
    single<AlmacenSeguro> { AlmacenSeguroIos() }
    single(named("esDepuracion")) { false }
}
