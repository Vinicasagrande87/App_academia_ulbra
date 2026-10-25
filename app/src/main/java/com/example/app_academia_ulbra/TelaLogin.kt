package com.example.app_academia_ulbra

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import com.example.app_academia_ulbra.dados.BancoLocal
import com.example.app_academia_ulbra.dados.LoginRequest
import com.example.app_academia_ulbra.dados.Sessao
import com.example.app_academia_ulbra.dados.mensagemDeErro
import com.example.app_academia_ulbra.ui.theme.Espaco
import kotlinx.coroutines.launch

@Composable
fun TelaLogin(aoEntrar: () -> Unit) {
    val context = LocalContext.current
    val escopo = rememberCoroutineScope()

    var email by rememberSaveable { mutableStateOf("") }
    var senha by rememberSaveable { mutableStateOf("") }
    var carregando by rememberSaveable { mutableStateOf(false) }

    fun fazerLogin() {
        if (carregando) return
        carregando = true
        escopo.launch {
            try {
                val resposta = BancoLocal.login(LoginRequest(email.trim(), senha))
                Sessao.entrar(resposta)
                aoEntrar()
            } catch (e: Exception) {
                mostrarToast(context, mensagemDeErro(e, "Login não cadastrado."))
            } finally {
                carregando = false
            }
        }
    }

    ConteudoTela {
        Text(
            "Academia Ulbra",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Espaco.grande)
        )
        Text(
            "Faça login para acessar o sistema",
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Espaco.medio)
        )

        CampoTexto(email, { email = it }, "E-mail", "seu@email.com", KeyboardType.Email)
        CampoTexto(senha, { senha = it }, "Senha", "Sua senha", KeyboardType.Password, senha = true)

        Button(
            onClick = { fazerLogin() },
            enabled = !carregando && email.isNotBlank() && senha.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Espaco.medio)
        ) {
            Text(if (carregando) "Entrando..." else "ENTRAR")
        }
    }
}
