package com.example.app_academia_ulbra

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Navegacao(){
    val navController = rememberNavController()
    var tipoLogado by rememberSaveable {mutableStateOf("")}
    var nomeLogado by rememberSaveable {mutableStateOf("")}
    var fotoPerfil by rememberSaveable {mutableStateOf("") }
    val emailLogado by rememberSaveable {mutableStateOf("")}
}