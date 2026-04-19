package com.example.riegon.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// --- TEMA CLARO ---
private val LightColorScheme = lightColorScheme(
    primary = VerdeInstitucional,        // Color de la barra superior y botones activos
    onPrimary = BlancoInstitucional,     // Color del texto encima de los botones
    secondary = RojoInstitucional,       // Color para switches o botones de parada
    onSecondary = BlancoInstitucional,
    tertiary = AmarilloInstitucional,
    background = FondoClaro,
    surface = BlancoInstitucional,
    onSurface = TextoOscuro
)

// --- TEMA OSCURO (Por si el estudiante tiene el celular en modo oscuro) ---
private val DarkColorScheme = darkColorScheme(
    primary = VerdeClaroDark,
    onPrimary = TextoOscuro,
    secondary = RojoClaroDark,
    onSecondary = TextoOscuro,
    tertiary = AmarilloClaroDark,
    background = TextoOscuro,
    surface = Color(0xFF2C2C2C),
    onSurface = BlancoInstitucional
)

@Composable
fun RiegonTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Apagamos los colores dinámicos de Android para forzar que SIEMPRE se usen los de la institución
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb() // Pinta la barra del reloj del celular
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Asume que tienes un archivo Type.kt por defecto
        content = content
    )
}