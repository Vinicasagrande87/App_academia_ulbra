package com.example.app_academia_ulbra

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Navegacao() {
    val navController = rememberNavController()
    var tipoLogado by rememberSaveable { mutableStateOf("") }
    var nomeLogado by rememberSaveable { mutableStateOf("") }
    val entradaAtual by navController.currentBackStackEntryAsState()
    val rotaAtual = entradaAtual?.destination?.route
    val mostrarBarraDeCima = rotaAtual != null && rotaAtual != "inicial"
    val mostrarBarraDeBaixo = rotaAtual != null && rotaAtual !in listOf("inicial", "login")

    Scaffold(
        topBar = {
            if (mostrarBarraDeCima) {
                TopAppBar(
                    title = { Text(tituloDa(rotaAtual)) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        },
        bottomBar = {
            if (mostrarBarraDeBaixo) {
                NavigationBar {
                    NavigationBarItem(
                        selected = rotaAtual == "alunos-treinos",
                        onClick = { irPelaBarra(navController, "alunos-treinos") },
                        icon = { Icon(Icons.Default.Star, contentDescription = "Treinos") },
                        label = { Text("Treinos") }
                    )
                    NavigationBarItem(
                        selected = rotaAtual == "posicao-financeira",
                        onClick = { irPelaBarra(navController, "posicao-financeira") },
                        icon = { Icon(Icons.Default.Star, contentDescription = "Financeiro") },
                        label = { Text("Financeiro") }
                    )
                    NavigationBarItem(
                        selected = rotaAtual == "sobre",
                        onClick = { irPelaBarra(navController, "sobre") },
                        icon = { Icon(Icons.Default.Info, contentDescription = "Sobre") },
                        label = { Text("Sobre") }
                    )
                }
            }
        },
        contentWindowInsets = if (mostrarBarraDeCima) ScaffoldDefaults.contentWindowInsets else WindowInsets(0)
    ) { innerPadding ->
        NavHost (
            navController = navController,
            startDestination = "inicial",
            modifier = Modifier.padding(innerPadding)


        ){
            composable ("inicial"){
                TelaInicial (aoTerminar = {
                    navController.navigate("login"){
                        popUpTo ("inicial"){inclusive = true  }
                    }
                })
            }
            composable ("login"){
                TelaLogin( aoEntrar = { nome, tipo ->
                    nomeLogado = nome
                    tipoLogado = tipo
                    navController.navigate("home-aluno"){
                        popUpTo ("login"){ inclusive = true  }
                    }})
            }
        }

    }
}

private fun tituloDa(rota: String?): String = when (rota) {
    "login" -> "Login"
    "alunos-treinos" -> "Meus Treinos"
    "posicao-financeira" -> "Financeiro"
    "sobre" -> "Sobre"
    else -> "Ulbra Gym"
}

private fun irPelaBarra(navController: NavHostController, rota: String) {
    navController.navigate(rota) {
        launchSingleTop = true
    }
}