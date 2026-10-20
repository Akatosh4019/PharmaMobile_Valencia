package pe.edu.upeu.pharmamobile.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.core.qualifier.named
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.android.ext.koin.androidContext
import android.content.pm.ApplicationInfo
import pe.edu.upeu.pharmamobile.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobile.data.local.AlmacenSeguroAndroid
import pe.edu.upeu.pharmamobile.domain.platform.Compartidor
import pe.edu.upeu.pharmamobile.platform.CompartidorAndroid
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import pe.edu.upeu.pharmamobile.data.local.PharmaDatabase

actual val platformModule: Module = module {
    single<SqlDriver> { AndroidSqliteDriver(PharmaDatabase.Schema, androidContext(), "pharmamobile.db") }
    single<HttpClientEngine> { OkHttp.create() }
    single(named("apiBaseUrl")) { "http://10.0.2.2:8080/api/v1/" }
    single<Compartidor> { CompartidorAndroid(get()) }
    single<AlmacenSeguro> { AlmacenSeguroAndroid(androidContext()) }
    single(named("esDepuracion")) {
        androidContext().applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
    }
}
