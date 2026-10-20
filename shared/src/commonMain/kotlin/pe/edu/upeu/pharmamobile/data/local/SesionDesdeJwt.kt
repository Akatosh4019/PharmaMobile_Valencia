package pe.edu.upeu.pharmamobile.data.local

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import pe.edu.upeu.pharmamobile.domain.model.Sesion
import kotlin.io.encoding.Base64
import kotlin.time.Clock

/** Recupera una sesión previamente autenticada solo mientras su JWT guardado siga vigente. */
internal fun sesionVigenteSinRed(token: String): Sesion? = runCatching {
    val partes = token.split('.')
    if (partes.size != 3) return null
    val cuerpo = Json.parseToJsonElement(Base64.UrlSafe.decode(partes[1]).decodeToString()).jsonObject
    val vencimiento = cuerpo["exp"]?.jsonPrimitive?.content?.toLongOrNull() ?: return null
    if (vencimiento <= Clock.System.now().epochSeconds) return null
    val usuario = cuerpo["sub"]?.jsonPrimitive?.content?.takeIf(String::isNotBlank) ?: return null
    val permisos = cuerpo["authorities"]?.jsonArray?.map { it.jsonPrimitive.content }?.toSet()
        ?: emptySet()
    Sesion(token, usuario, usuario, permisos)
}.getOrNull()
