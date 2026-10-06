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
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobile.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.ObtenerProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.EliminarProductoUseCase
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoViewModel

val repositoryModule = module {
    single { crearHttpClient(get(), get(named("apiBaseUrl"))) }
    single { ProductoApi(get()) }
    single<ProductoRepository> { ProductoRepositorioRemoto(get()) }
}

val useCaseModule = module {
    factory { RegistrarProductoUseCase(get()) }
    factory { ObtenerProductoUseCase(get()) }
    factory { ActualizarProductoUseCase(get()) }
    factory { EliminarProductoUseCase(get()) }
}

val viewModelModule = module {
    viewModel { ProductoViewModel(get(), get(), get(), get(), get()) }
}

expect val platformModule: Module

fun initKoin(config: KoinApplication.() -> Unit = {}): KoinApplication =
    startKoin {
        config()
        modules(repositoryModule, useCaseModule, viewModelModule, platformModule)
    }
