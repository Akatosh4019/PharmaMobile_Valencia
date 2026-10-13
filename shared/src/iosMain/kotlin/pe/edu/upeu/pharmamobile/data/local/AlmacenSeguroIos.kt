package pe.edu.upeu.pharmamobile.data.local

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import platform.CoreFoundation.CFDataCreate
import platform.CoreFoundation.CFDataGetBytePtr
import platform.CoreFoundation.CFDataGetLength
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionarySetValue
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringCreateWithCString
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFStringEncodingUTF8
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecItemNotFound
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleWhenUnlockedThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

/** Guarda únicamente el JWT en Keychain; nunca en NSUserDefaults. */
@OptIn(ExperimentalForeignApi::class)
class AlmacenSeguroIos : AlmacenSeguro {
    private val servicio = "pe.edu.upeu.pharmamobile.auth"
    private val cuenta = "jwt"

    override fun guardarToken(token: String) = memScoped {
        borrarToken()
        val (consulta, servicioCf, cuentaCf) = consultaBase()
        val bytes = token.encodeToByteArray()
        val datos = bytes.usePinned { CFDataCreate(null, it.addressOf(0), bytes.size.toLong()) }
            ?: error("No se pudo preparar el token para Keychain")
        try {
            CFDictionarySetValue(consulta, kSecValueData, datos)
            CFDictionarySetValue(consulta, kSecAttrAccessible, kSecAttrAccessibleWhenUnlockedThisDeviceOnly)
            check(SecItemAdd(consulta, null) == errSecSuccess) { "No se pudo guardar el token en Keychain" }
        } finally {
            CFRelease(datos)
            CFRelease(consulta)
            CFRelease(servicioCf)
            CFRelease(cuentaCf)
        }
    }

    override fun leerToken(): String? = memScoped {
        val (consulta, servicioCf, cuentaCf) = consultaBase()
        try {
            CFDictionarySetValue(consulta, kSecReturnData, kCFBooleanTrue)
            CFDictionarySetValue(consulta, kSecMatchLimit, kSecMatchLimitOne)
            val resultado = alloc<CFTypeRefVar>()
            if (SecItemCopyMatching(consulta, resultado.ptr) != errSecSuccess) return@memScoped null
            val datos = resultado.value ?: return@memScoped null
            try {
                val longitud = CFDataGetLength(datos).toInt()
                val puntero = CFDataGetBytePtr(datos) ?: return@memScoped null
                ByteArray(longitud) { puntero[it].toByte() }.decodeToString()
            } finally {
                CFRelease(datos)
            }
        } finally {
            CFRelease(consulta)
            CFRelease(servicioCf)
            CFRelease(cuentaCf)
        }
    }

    override fun borrarToken() {
        val (consulta, servicioCf, cuentaCf) = consultaBase()
        try {
            val estado = SecItemDelete(consulta)
            check(estado == errSecSuccess || estado == errSecItemNotFound) {
                "No se pudo borrar el token de Keychain"
            }
        } finally {
            CFRelease(consulta)
            CFRelease(servicioCf)
            CFRelease(cuentaCf)
        }
    }

    private fun consultaBase() = memScoped {
        val consulta = CFDictionaryCreateMutable(null, 0, null, null)
            ?: error("No se pudo crear la consulta de Keychain")
        val servicioCf = CFStringCreateWithCString(null, servicio.cstr.ptr, kCFStringEncodingUTF8)
            ?: error("Servicio de Keychain inválido")
        val cuentaCf = CFStringCreateWithCString(null, cuenta.cstr.ptr, kCFStringEncodingUTF8)
            ?: error("Cuenta de Keychain inválida")
        CFDictionarySetValue(consulta, kSecClass, kSecClassGenericPassword)
        CFDictionarySetValue(consulta, kSecAttrService, servicioCf)
        CFDictionarySetValue(consulta, kSecAttrAccount, cuentaCf)
        // Los callbacks nulos no retienen valores; los tres objetos se liberan juntos.
        Triple(consulta, servicioCf, cuentaCf)
    }
}
