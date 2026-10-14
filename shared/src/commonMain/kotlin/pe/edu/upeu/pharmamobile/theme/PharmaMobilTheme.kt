package pe.edu.upeu.pharmamobile.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = Color(0xFF08766C),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9F3ED),
    onPrimaryContainer = Color(0xFF064A45),
    secondary = Color(0xFF52726C),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE5F1ED),
    onSecondaryContainer = Color(0xFF183D37),
    tertiary = Color(0xFF446A85),
    onTertiary = Color.White,
    background = Color(0xFFF4F8F7),
    onBackground = Color(0xFF142623),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF142623),
    onSurfaceVariant = Color(0xFF657773),
    surfaceContainer = Color(0xFFF0F5F3),
    outline = Color(0xFFBACBC6),
    error = Color(0xFFB3261E),
    errorContainer = Color(0xFFFFE9E6),
    onErrorContainer = Color(0xFF842017)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF79D8C8),
    onPrimary = Color(0xFF063F39),
    primaryContainer = Color(0xFF175C54),
    onPrimaryContainer = Color(0xFFCBF5EC),
    secondary = Color(0xFFB1CCC5),
    onSecondary = Color(0xFF1C3530),
    secondaryContainer = Color(0xFF334B47),
    onSecondaryContainer = Color(0xFFCDE8E1),
    tertiary = Color(0xFFACCBE6),
    onTertiary = Color(0xFF143349),
    background = Color(0xFF0E1917),
    onBackground = Color(0xFFE5F0ED),
    surface = Color(0xFF172522),
    onSurface = Color(0xFFE5F0ED),
    onSurfaceVariant = Color(0xFFADC2BC),
    surfaceContainer = Color(0xFF21332F),
    outline = Color(0xFF647A73),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF5E2522),
    onErrorContainer = Color(0xFFFFDAD5)
)

private val PharmaTypography = Typography(
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp)
)

private val PharmaShapes = Shapes(
    small = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
)

@Composable
fun PharmaMobilTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {

    val colors = if (darkTheme) {
        DarkColors
    } else {
        LightColors
    }

    MaterialTheme(
        colorScheme = colors,
        typography = PharmaTypography,
        shapes = PharmaShapes,
        content = content
    )
}
