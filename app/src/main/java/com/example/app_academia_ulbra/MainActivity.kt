package com.example.app_academia_ulbra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.app_academia_ulbra.dados.Sessao
import com.example.app_academia_ulbra.ui.theme.TemaApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Sessao.iniciar(this)
        enableEdgeToEdge()
        setContent {
            TemaApp {
                Navegacao()
            }
        }
    }
}
