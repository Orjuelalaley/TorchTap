package com.example.torchtap

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Acento ámbar de la marca, compartido por ambos temas.
private val Amber = Color(0xFFFFB300)
private val AmberDark = Color(0xFFF29900)

// Tema oscuro (por defecto): tonos fríos y cálidos que ya usaba la linterna.
// `surface` == `background` a propósito: así el header, el menú lateral y el
// fondo comparten el mismo tono y no se ve ninguna línea de división.
private val DarkBackground = Color(0xFF0E1116)
private val DarkColors = darkColorScheme(
    primary = Amber,
    onPrimary = Color(0xFF1A1A1A),
    secondary = Color(0xFFFFC107),
    onSecondary = Color(0xFF1A1A1A),
    background = DarkBackground,
    onBackground = Color(0xFFE6E8EC),
    surface = DarkBackground,
    onSurface = Color(0xFFE6E8EC),
    surfaceVariant = Color(0xFF222831),
    onSurfaceVariant = Color(0xFF9AA1AD),
    outline = Color(0xFF3A3F48),
)

// Tema claro: mismos acentos ámbar sobre fondos cálidos y claros.
// Igual que en oscuro, `surface` == `background` para que no haya divisiones.
private val LightBackground = Color(0xFFFDF9F2)
private val LightColors = lightColorScheme(
    primary = AmberDark,
    onPrimary = Color(0xFF1A1A1A),
    secondary = Color(0xFFF2A600),
    onSecondary = Color(0xFF1A1A1A),
    background = LightBackground,
    onBackground = Color(0xFF1B1B1F),
    surface = LightBackground,
    onSurface = Color(0xFF1B1B1F),
    surfaceVariant = Color(0xFFEDE6D8),
    onSurfaceVariant = Color(0xFF5A5750),
    outline = Color(0xFFCFC8BA),
)

/** Tema de la app. [darkTheme] alterna entre el esquema oscuro y el claro. */
@Composable
fun TorchTapTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
