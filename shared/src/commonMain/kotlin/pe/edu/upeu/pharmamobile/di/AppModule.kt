package pe.edu.upeu.pharmamobile.di

import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.koin.core.qualifier.named
import pe.edu.upeu.pharmamobile.data.remote.ProductoApi
import pe.edu.upeu.pharmamobile.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobile.data.repository.ProductoRepositorioRemoto
import pe.edu.upeu.pharmamobile.data.repository.ProductoRepositorioOffline
import pe.edu.upeu.pharmamobile.data.local.PharmaDatabase
import app.cash.sqldelight.db.SqlDriver
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobile.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.ObtenerProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.EliminarProductoUseCase
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoViewModel
import pe.edu.upeu.pharmamobile.data.local.EstadoSesion
import pe.edu.upeu.pharmamobile.data.repository.AuthRepositorioRest
import pe.edu.upeu.pharmamobile.domain.repository.AuthRepository
import pe.edu.upeu.pharmamobile.domain.usecase.IniciarSesionUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.CerrarSesionUseCase
import pe.edu.upeu.pharmamobile.presentation.sesion.SesionViewModel

val repositoryModule = module {
    single { EstadoSesion(get()) }
    single { crearHttpClient(get(), get(named("apiBaseUrl")), get(), get(), get(named("esDepuracion"))) }
    single<AuthRepository> { AuthRepositorioRest(get(), get(), get()) }
    single { ProductoApi(get()) }
    single { PharmaDatabase(get<SqlDriver>()) }
    single<ProductoRepository> { ProductoRepositorioOffline(ProductoRepositorioRemoto(get()), get()) }
}

val useCaseModule = module {
    factory { IniciarSesionUseCase(get()) }
    factory { CerrarSesionUseCase(get()) }
    factory { RegistrarProductoUseCase(get()) }
    factory { ObtenerProductoUseCase(get()) }
    factory { ActualizarProductoUseCase(get()) }
    factory { EliminarProductoUseCase(get()) }
}

val viewModelModule = module {
    viewModel { SesionViewModel(get(), get(), get(), get()) }
    viewModel { ProductoViewModel(get(), get(), get(), get(), get()) }
}

expect val platformModule: Module

fun initKoin(config: KoinApplication.() -> Unit = {}): KoinApplication =
    startKoin {
        config()
        modules(repositoryModule, useCaseModule, viewModelModule, platformModule)
    }
