package com.example.app_academia_ulbra

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.app_academia_ulbra.ui.theme.VerdeUlbra
import kotlinx.coroutines.delay

// mesmos tempos da splash do site (app.component.ts)
private const val ATRASO_LOGO_MS = 2000L
private const val DURACAO_MINIMA_MS = 3100L

@Composable
fun TelaInicial(aoTerminar: () -> Unit) {
    var logoVisivel by remember { mutableStateOf(false) }

    val opacidade by animateFloatAsState(if (logoVisivel) 1f else 0f, tween(560), label = "opacidade")
    val escala by animateFloatAsState(if (logoVisivel) 1f else 0.9f, tween(560), label = "escala")

    LaunchedEffect(Unit) {
        delay(ATRASO_LOGO_MS)
        logoVisivel = true
        delay(DURACAO_MINIMA_MS - ATRASO_LOGO_MS)
        aoTerminar()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(VerdeUlbra, Color(0xFF00514C)))),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.logo_ulbra_gym),
            contentDescription = "Ulbra Gym",
            modifier = Modifier
                .widthIn(max = 460.dp)
                .fillMaxWidth(0.82f)
                .alpha(opacidade)
                .scale(escala)
        )
    }
}
