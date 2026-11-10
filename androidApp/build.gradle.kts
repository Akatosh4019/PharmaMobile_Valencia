import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.isFile) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

// Los plugins de Firebase se activan al añadir la configuración descargada desde la consola.
if (file("google-services.json").isFile) {
    apply(plugin = "com.google.gms.google-services")
    apply(plugin = "com.google.firebase.crashlytics")
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(project(":shared"))

    implementation(libs.androidx.activity.compose)

    implementation(libs.compose.uiToolingPreview)
    implementation(libs.koin.android)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "pe.edu.upeu.pharmamobile"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "pe.edu.upeu.pharmamobile"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0.0"
        manifestPlaceholders["apiBaseUrl"] = "http://10.0.2.2:8080/api/v1/"
    }
    flavorDimensions += "backend"
    productFlavors {
        create("emulator") {
            dimension = "backend"
            manifestPlaceholders["apiBaseUrl"] = "http://10.0.2.2:8080/api/v1/"
        }
        create("phone") {
            dimension = "backend"
            manifestPlaceholders["apiBaseUrl"] = "http://172.17.26.36:8080/api/v1/"
        }
    }
    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.isFile) {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}
