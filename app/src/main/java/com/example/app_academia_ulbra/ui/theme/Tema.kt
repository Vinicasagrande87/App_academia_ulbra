package com.example.app_academia_ulbra.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val VerdeUlbra = Color(0xFF025E55)
val DouradoUlbra = Color(0xFFB8863A)

// cores de aviso, as mesmas do Ionic que o site usa (success, warning, danger, medium)
val VerdeSucesso = Color(0xFF2DD36F)
val AmareloAtencao = Color(0xFFFFC409)
val VermelhoPerigo = Color(0xFFEB445A)
val CinzaNeutro = Color(0xFF92949C)

private val CoresApp = lightColorScheme(
    primary = VerdeUlbra,
    onPrimary = Color.White,
    secondary = DouradoUlbra,
    onSecondary = Color.White,
    error = VermelhoPerigo,
    onError = Color.White,
    surfaceVariant = Color(0xFFF4F5F8),
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black
)

object Espaco{
    val pequeno = 8.dp
    val medio = 16.dp
    val grande = 24.dp
}

@Composable
fun TemaApp (conteudo: @Composable () -> Unit){
    MaterialTheme(
        colorScheme = CoresApp,
        shapes = Shapes(
            small = RoundedCornerShape(4.dp),
            medium = RoundedCornerShape(4.dp)
        ),
        content = conteudo
    )
}

