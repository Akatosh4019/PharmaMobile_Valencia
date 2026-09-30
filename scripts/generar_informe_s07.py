"""Genera el informe verificable de la actividad autónoma S07."""

from __future__ import annotations

import argparse
from pathlib import Path

from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import cm
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import (
    BaseDocTemplate,
    Frame,
    Image,
    KeepTogether,
    PageBreak,
    PageTemplate,
    Paragraph,
    Preformatted,
    Spacer,
    Table,
    TableStyle,
)


ROOT = Path(__file__).resolve().parents[1]
EVIDENCE = ROOT / "evidencias" / "s07_autonoma"
OUTPUT = ROOT / "output" / "pdf" / "S07_ActividadAutonoma_Valencia_Saavedra.pdf"
BLUE = colors.HexColor("#12497C")
TEAL = colors.HexColor("#00695C")
PALE = colors.HexColor("#EAF1F5")
DARK = colors.HexColor("#16283A")
GREY = colors.HexColor("#506070")


def fonts() -> None:
    pdfmetrics.registerFont(TTFont("ArialS07", "C:/Windows/Fonts/arial.ttf"))
    pdfmetrics.registerFont(TTFont("ArialS07-Bold", "C:/Windows/Fonts/arialbd.ttf"))
    pdfmetrics.registerFontFamily(
        "ArialS07", normal="ArialS07", bold="ArialS07-Bold"
    )


def styles():
    base = getSampleStyleSheet()
    base.add(ParagraphStyle(name="TitleS07", fontName="ArialS07-Bold", fontSize=21, leading=27, textColor=BLUE, spaceAfter=12))
    base.add(ParagraphStyle(name="SectionS07", fontName="ArialS07-Bold", fontSize=13, leading=17, textColor=BLUE, spaceBefore=10, spaceAfter=8))
    base.add(ParagraphStyle(name="SubS07", fontName="ArialS07-Bold", fontSize=10, leading=14, textColor=TEAL, spaceBefore=8, spaceAfter=5))
    base.add(ParagraphStyle(name="BodyS07", fontName="ArialS07", fontSize=9, leading=13.5, textColor=DARK, spaceAfter=7))
    base.add(ParagraphStyle(name="SmallS07", fontName="ArialS07", fontSize=7.5, leading=10.5, textColor=DARK, spaceAfter=4))
    base.add(ParagraphStyle(name="CaptionS07", fontName="ArialS07", fontSize=7.5, leading=10, textColor=GREY, alignment=TA_CENTER, spaceAfter=8))
    base.add(ParagraphStyle(name="HeadCellS07", fontName="ArialS07-Bold", fontSize=7.2, leading=9, textColor=colors.white))
    base.add(ParagraphStyle(name="CellS07", fontName="ArialS07", fontSize=7, leading=9, textColor=DARK))
    return base


def p(text: str, sty="BodyS07"):
    return Paragraph(text, STYLES[sty])


def table(headers, rows, widths):
    data = [[p(x, "HeadCellS07") for x in headers]]
    data.extend([[p(str(x), "CellS07") for x in row] for row in rows])
    result = Table(data, colWidths=widths, repeatRows=1, hAlign="LEFT")
    result.setStyle(TableStyle([
        ("BACKGROUND", (0, 0), (-1, 0), BLUE),
        ("ROWBACKGROUNDS", (0, 1), (-1, -1), [colors.white, PALE]),
        ("GRID", (0, 0), (-1, -1), 0.3, colors.HexColor("#CFDAE2")),
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ("LEFTPADDING", (0, 0), (-1, -1), 5),
        ("RIGHTPADDING", (0, 0), (-1, -1), 5),
        ("TOPPADDING", (0, 0), (-1, -1), 5),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 5),
    ]))
    return result


def screenshot(name, caption, max_height=14 * cm):
    path = EVIDENCE / name
    image = Image(str(path))
    scale = min(17.2 * cm / image.imageWidth, max_height / image.imageHeight)
    image.drawWidth = image.imageWidth * scale
    image.drawHeight = image.imageHeight * scale
    image.hAlign = "CENTER"
    return [image, p(caption, "CaptionS07")]


def page(canvas, doc):
    canvas.saveState()
    w, h = A4
    canvas.setFillColor(BLUE)
    canvas.rect(0, h - 0.9 * cm, w, 0.9 * cm, fill=1, stroke=0)
    canvas.setFont("ArialS07-Bold", 8)
    canvas.setFillColor(colors.white)
    canvas.drawString(1.7 * cm, h - 0.55 * cm, "PHARMAMOBIL  /  ACTIVIDAD AUTÓNOMA 07")
    canvas.setStrokeColor(colors.HexColor("#CFDAE2"))
    canvas.line(1.7 * cm, 1.3 * cm, w - 1.7 * cm, 1.3 * cm)
    canvas.setFillColor(GREY)
    canvas.setFont("ArialS07", 7)
    canvas.drawString(1.7 * cm, 0.9 * cm, "Valencia Saavedra · Desarrollo de Aplicaciones Móviles · 2026-2")
    canvas.drawRightString(w - 1.7 * cm, 0.9 * cm, f"Página {doc.page}")
    canvas.restoreState()


def make_report(student: str, commit: str):
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    doc = BaseDocTemplate(str(OUTPUT), pagesize=A4, leftMargin=1.7 * cm, rightMargin=1.7 * cm, topMargin=1.4 * cm, bottomMargin=1.6 * cm)
    frame = Frame(1.7 * cm, 1.6 * cm, A4[0] - 3.4 * cm, A4[1] - 3.2 * cm, leftPadding=0, rightPadding=0, topPadding=0, bottomPadding=0)
    doc.addPageTemplates(PageTemplate(id="main", frames=[frame], onPage=page))
    story = []

    # Portada y alcance.
    story += [Spacer(1, 1.0 * cm), p("Endpoints, DTO y pruebas de conexión", "TitleS07"),
              p("Actividad autónoma N.º 07 · Sesión 7", "SectionS07"),
              p(f"<b>Estudiante:</b> {student}<br/><b>Asignatura:</b> Desarrollo de Aplicaciones Móviles<br/><b>Fecha de pruebas:</b> 29 de setiembre de 2026<br/><b>Plataforma verificada:</b> Android Emulator, API 37.1"),
              p("Proyecto: PharmaMobil (Kotlin Multiplatform / Compose / Ktor) y backend PharmaSoft (Spring Boot / Oracle). La evidencia documenta el comportamiento observado; no sustituye un resultado parcial por uno supuesto."),
              p("Repositorio Android", "SubS07"),
              p('<link href="https://github.com/Akatosh4019/PharmaMobile_Valencia">github.com/Akatosh4019/PharmaMobile_Valencia</link>'),
              p("Repositorio backend", "SubS07"),
              p('<link href="https://github.com/Akatosh4019/BackendPharmobile">github.com/Akatosh4019/BackendPharmobile</link>'),
              p("Alcance y criterio de evidencia", "SectionS07"),
              p("Se documentan cinco escenarios ejecutados en Android. El docente dispensó la evidencia de iOS; por ello no se declara una ejecución en esa plataforma. Los cambios temporales para provocar 404, timeout y JSON desconocido se revirtieron al finalizar."),
              table(["Escenario", "Evidencia observada"], [
                  ["GET exitoso", "GET /productos y /categorias → HTTP 200; lista visible."],
                  ["ID inexistente", "GET /productos/999999 → HTTP 404; error capturado, mensaje técnico."],
                  ["Sin conexión", "Modo avión → error de conexión; reintento posterior correcto."],
                  ["Timeout", "requestTimeoutMillis = 1 → vencimiento; app sigue abierta."],
                  ["JSON desconocido", "ignoreUnknownKeys = false → JsonConvertException; con true funciona."],
              ], [4.4 * cm, 12.8 * cm]), PageBreak()]

    # Catálogo.
    story += [p("1. Catálogo de endpoints", "SectionS07"),
              p("API base: <b>http://10.0.2.2:8080/api/v1/</b> desde Android Emulator; <b>http://localhost:8080/api/v1/</b> desde el PC. Versión: <b>v1</b>. Los códigos describen el contrato y las pruebas efectuadas; 401 no se declara porque esta API local no usa autenticación en estas rutas."),
              table(["Método / ruta", "Parámetros", "Respuesta", "Errores relevantes"], [
                  ["GET /productos", "pagina, tamanio (query; 0, 20 por defecto)", "200 · PaginaProductosDto", "400 en parámetros inválidos; 500 servidor"],
                  ["GET /productos/{id}", "id (ruta)", "200 · ProductoDto", "404 inexistente (probado); 400 id inválido"],
                  ["POST /productos", "ProductoRequestDto (JSON)", "201 · ProductoDto, ID generado", "400 validación/categoría inválida"],
                  ["PUT /productos/{id}", "id + ProductoRequestDto", "200 · ProductoDto", "400 validación; 404 inexistente"],
                  ["DELETE /productos/{id}", "id (ruta)", "204 · sin cuerpo; baja lógica", "404 inexistente"],
                  ["GET /categorias", "Sin parámetros", "200 · lista de CategoriaDto", "500 servidor"],
              ], [4.2 * cm, 4.4 * cm, 4.3 * cm, 4.3 * cm]),
              Spacer(1, 0.4 * cm),
              p("El cliente de Android consume GET /productos, GET /categorias, POST, PUT y DELETE. GET /productos/{id} existe en el backend y se ejecutó para la prueba controlada de 404 mediante una modificación temporal de la URL en ProductoApi; no hay botón de búsqueda por ID en la interfaz final."),
              p("DELETE no elimina físicamente el registro: el backend marca estado = false. La app lo muestra en la pestaña Inactivos tras volver a consultar la lista. El contrato real devuelve 204, no 200."),
              PageBreak()]

    # Diccionario.
    story += [p("2. Diccionario de DTO", "SectionS07"),
              p("Las tablas reproducen la nulabilidad y los valores por defecto declarados en Kotlin. «Sí» indica un argumento requerido para construir el DTO; los campos con valor predeterminado no lo son."),
              p("ProductoDto → Producto", "SubS07"),
              table(["JSON", "Tipo Kotlin", "Oblig.", "Defecto", "Dominio"], [
                  ["id", "Long", "Sí", "—", "Producto.id"], ["nombre", "String", "Sí", "—", "Producto.nombre"],
                  ["precio", "Double", "Sí", "—", "Producto.precio"], ["stock", "Int", "Sí", "—", "Producto.stock"],
                  ["estado", "Boolean", "No", "true", "Producto.activo"], ["categoriaId", "Long?", "No", "null", "Producto.categoriaId"],
                  ["categoriaNombre", "String?", "No", "null", "Producto.categoriaNombre"],
              ], [3.7 * cm, 3 * cm, 1.8 * cm, 2.2 * cm, 6.5 * cm]),
              p("PaginaProductosDto → colección de Producto", "SubS07"),
              table(["JSON", "Tipo Kotlin", "Oblig.", "Defecto", "Uso"], [
                  ["contenido", "List&lt;ProductoDto&gt;", "Sí", "—", "Se mapea a List&lt;Producto&gt;"],
                  ["pagina", "Int", "Sí", "—", "Paginación; no pasa al modelo"],
                  ["tamanio", "Int", "Sí", "—", "Paginación; no pasa al modelo"],
                  ["totalElementos", "Long", "Sí", "—", "Metadato; no pasa al modelo"],
                  ["totalPaginas", "Int", "Sí", "—", "Metadato; no pasa al modelo"],
                  ["ultima", "Boolean", "Sí", "—", "Controla el bucle de carga"],
              ], [3.7 * cm, 3 * cm, 1.8 * cm, 2.2 * cm, 6.5 * cm]),
              PageBreak()]

    story += [p("2. Diccionario de DTO (continuación)", "SectionS07"),
              p("ProductoRequestDto → datos enviados", "SubS07"),
              table(["JSON", "Tipo Kotlin", "Oblig.", "Defecto", "Origen en dominio"], [
                  ["nombre", "String", "Sí", "—", "Producto.nombre"], ["precio", "Double", "Sí", "—", "Producto.precio"],
                  ["stock", "Int", "Sí", "—", "Producto.stock"], ["estado", "Boolean", "Sí", "—", "Producto.activo"],
                  ["categoriaId", "Long", "Sí", "—", "Producto.categoriaId (no nulo)"],
              ], [3.7 * cm, 3 * cm, 1.8 * cm, 2.2 * cm, 6.5 * cm]),
              p("CategoriaDto → Categoria", "SubS07"),
              table(["JSON", "Tipo Kotlin", "Oblig.", "Defecto", "Dominio"], [
                  ["id", "Long", "Sí", "—", "Categoria.id"], ["nombre", "String", "Sí", "—", "Categoria.nombre"],
              ], [3.7 * cm, 3 * cm, 1.8 * cm, 2.2 * cm, 6.5 * cm]),
              p("Fragmento JSON real del backend (GET /productos)", "SubS07"),
              Preformatted('''{
  "contenido": [{
    "id": 1, "nombre": "Paracetamol", "precio": 15.5,
    "stock": 100, "estado": true, "categoriaId": 1,
    "categoriaNombre": "Medicamentos",
    "fechaCreacion": "2026-09-29T20:04:30.325073"
  }],
  "pagina": 0, "tamanio": 20, "totalElementos": 7,
  "totalPaginas": 1, "ultima": true
}''', ParagraphStyle(name="CodeS07", fontName="Courier", fontSize=7.7, leading=10.5, textColor=DARK, backColor=PALE, borderPadding=9)),
              p("Este fragmento abrevia el arreglo de siete productos, pero conserva los nombres y tipos devueltos. fechaCreacion es un campo adicional del backend; ignoreUnknownKeys = true permite omitirlo sin romper ProductoDto."),
              p("Código DTO correspondiente", "SubS07"),
              Preformatted('''@Serializable data class ProductoDto(
  val id: Long, val nombre: String, val precio: Double,
  val stock: Int, val estado: Boolean = true,
  val categoriaId: Long? = null,
  val categoriaNombre: String? = null,
)''', ParagraphStyle(name="Code2S07", fontName="Courier", fontSize=7.7, leading=10.5, textColor=DARK, backColor=PALE, borderPadding=9)),
              PageBreak()]

    def scenario(title, details, files):
        story.append(p(title, "SectionS07"))
        for label, value in details:
            story.append(p(f"<b>{label}:</b> {value}"))
        for name, caption, height in files:
            story.extend(screenshot(name, caption, height))
        story.append(PageBreak())

    scenario("3.1 Prueba 01 · Respuesta exitosa", [
        ("Fecha / plataforma", "29/09/2026, 20:40; Android Emulator API 37.1."),
        ("Pasos", "Con Docker y backend encendidos, abrir la app y pulsar Actualizar lista. Filtrar Logcat por tag:PharmaMobilHTTP."),
        ("Esperado", "HTTP 200 y lista de productos renderizada."),
        ("Observado", "GET /api/v1/productos?pagina=0&amp;tamanio=20 → 200; GET /categorias → 200. Se muestran productos con nombre, precio, stock y categoría."),
        ("Conclusión", "Conexión y deserialización normales verificadas."),
    ], [("01_get_200_logcat.png", "Evidencia 01A. Petición y respuesta 200 en Logcat.", 9.7 * cm),
        ("01_android_productos.png", "Evidencia 01B. Lista cargada en Android.", 8.5 * cm)])

    scenario("3.2 Prueba 02 · Recurso inexistente", [
        ("Fecha / plataforma", "29/09/2026, 20:50; Android Emulator API 37.1."),
        ("Pasos", "Cambiar temporalmente client.get(\"productos\") a client.get(\"productos/999999\") en ProductoApi, ejecutar de nuevo la app y consultar la lista."),
        ("Esperado", "HTTP 404, ClientRequestException capturada y mensaje controlado."),
        ("Observado", "Logcat registra GET /productos/999999 → 404. La app no se cierra, pero muestra el texto completo de la excepción y del JSON del backend; el mensaje no está simplificado."),
        ("Qué ve el usuario", "Error técnico largo con 404 y «Producto no encontrado»."),
        ("Conclusión", "Manejo sin cierre correcto; presentación del error mejorable."),
    ], [("02_404_logcat.png", "Evidencia 02A. URL inexistente y respuesta 404.", 10.5 * cm),
        ("02_404_app.png", "Evidencia 02B. Mensaje observado en la app.", 7.5 * cm)])

    scenario("3.3 Prueba 03 · Sin conexión y recuperación", [
        ("Fecha / plataforma", "29/09/2026, 13:45 (hora mostrada por el emulador); Android Emulator API 37.1."),
        ("Pasos", "Activar modo avión, pulsar Actualizar lista; desactivar modo avión y pulsar Reintentar."),
        ("Esperado", "Fallo de entrada/salida capturado, mensaje y recuperación tras reintento."),
        ("Observado", "La app muestra «Failed to connect to /10.0.2.2:8080» sin cerrarse. Tras reintentar, vuelve a listar los productos."),
        ("Qué ve el usuario", "Texto técnico de conexión y botón Reintentar; luego listado normal."),
        ("Conclusión", "Recuperación funcional; conviene simplificar el texto de error."),
    ], [("03_sin_conexion.png", "Evidencia 03A. Modo avión y error de conexión.", 10.0 * cm),
        ("03_recuperacion.png", "Evidencia 03B. Productos cargados tras recuperar conexión.", 9.0 * cm)])

    scenario("3.4 Prueba 04 · Tiempo de espera agotado", [
        ("Fecha / plataforma", "29/09/2026, 20:53; Android Emulator API 37.1."),
        ("Pasos", "Cambiar temporalmente requestTimeoutMillis de 15_000 a 1 en HttpClientFactory; ejecutar de nuevo y actualizar la lista."),
        ("Esperado", "Timeout manejado sin bloqueo de interfaz."),
        ("Observado", "Logcat registra «Request timeout has expired» para categorías y productos, con request_timeout=1 ms. El registro menciona CancellationException; la app muestra el vencimiento y sigue operativa."),
        ("Qué ve el usuario", "Error de timeout y opción de reintentar."),
        ("Conclusión", "Timeout reproducido; no se afirma HttpRequestTimeoutException porque la traza observada nombra CancellationException."),
    ], [("04_timeout_logcat.png", "Evidencia 04A. Timeout de 1 ms en Logcat.", 9.5 * cm),
        ("04_timeout_app.png", "Evidencia 04B. Interfaz abierta con error de timeout.", 10.0 * cm)])

    scenario("3.5 Prueba 05 · Campo JSON desconocido", [
        ("Fecha / plataforma", "29/09/2026, 21:59; Android Emulator API 37.1."),
        ("Pasos", "Cambiar temporalmente ignoreUnknownKeys de true a false, ejecutar de nuevo y actualizar la lista. Después restaurar true."),
        ("Esperado", "Con false, excepción de serialización; con true, lectura normal."),
        ("Observado", "El backend responde HTTP 200, pero Ktor registra JsonConvertException: categoria contiene descripcion y producto contiene fechaCreacion, campos no declarados en los DTO. Con true se obtuvo la lista en la prueba 01."),
        ("Qué ve el usuario", "Error técnico «Encountered an unknown key»; la app no se cierra."),
        ("Conclusión", "Se verificó la necesidad de ignorar campos adicionales en este contrato."),
    ], [("05_json_logcat.png", "Evidencia 05A. HTTP 200 seguido del fallo de deserialización.", 10.0 * cm),
        ("05_json_app.png", "Evidencia 05B. Error visible para el usuario.", 9.5 * cm)])

    # Referencias y cierre.
    story += [p("4. Repositorio, configuración final y conclusiones", "SectionS07"),
              p("Configuración final verificada en el código: client.get(\"productos\"), requestTimeoutMillis = 15_000 e ignoreUnknownKeys = true. README.md contiene la sección «Conectividad REST» y las instrucciones de ejecución con Docker."),
              p(f'<b>Commit de código y evidencias:</b> <link href="https://github.com/Akatosh4019/PharmaMobile_Valencia/commit/{commit}">{commit}</link>'),
              p("<b>Backend:</b> https://github.com/Akatosh4019/BackendPharmobile"),
              p("Los cinco escenarios fueron ejecutados. La app maneja las fallas sin cerrarse, aunque la presentación de 404, conexión, timeout y JSON aún muestra mensajes técnicos. Este resultado se declara como oportunidad de mejora y no como cumplimiento completo del criterio de mensajes controlados."),
              p("La captura de Logcat registra método, URL, cabeceras y estado; la configuración usa LogLevel.HEADERS, por lo que no imprime cuerpos JSON. El fragmento JSON de la sección 2 fue obtenido directamente del backend local."),
              p("Referencia de la actividad", "SubS07"),
              p("Universidad Peruana Unión. Actividad Autónoma N.º 07 — Documentación de endpoints, DTO y pruebas de conexión, sesión 7, Desarrollo de Aplicaciones Móviles, 2026-2."),
              ]
    if isinstance(story[-1], PageBreak):
        story.pop()
    doc.build(story)
    print(OUTPUT)


if __name__ == "__main__":
    fonts()
    STYLES = styles()
    parser = argparse.ArgumentParser()
    parser.add_argument("--student", required=True)
    parser.add_argument("--commit", required=True)
    args = parser.parse_args()
    make_report(args.student, args.commit)
