package com.example.app_academia_ulbra.dados

import android.content.Context
import android.content.SharedPreferences

// Guarda quem está logado, igual ao localStorage do site (token + id, nome e tipo).
// Fica salvo no aparelho, então o usuário continua logado ao reabrir o app
object Sessao {

    private lateinit var prefs: SharedPreferences

    var token: String? = null
        private set
    var id: Int = 0
        private set
    var nome: String = ""
        private set
    var tipo: String = ""
        private set

    val logado: Boolean get() = token != null
    val isAdmin: Boolean get() = tipo == "admin"
    val isAluno: Boolean get() = tipo == "aluno"

    fun iniciar(context: Context) {
        prefs = context.getSharedPreferences("sessao", Context.MODE_PRIVATE)
        token = prefs.getString("token", null)
        id = prefs.getInt("id", 0)
        nome = prefs.getString("nome", "") ?: ""
        tipo = prefs.getString("tipo", "") ?: ""
    }

    fun entrar(resposta: LoginResposta) {
        token = resposta.token
        id = resposta.id
        nome = resposta.nome
        tipo = resposta.tipo.lowercase()
        prefs.edit()
            .putString("token", token)
            .putInt("id", id)
            .putString("nome", nome)
            .putString("tipo", tipo)
            .apply()
    }

    fun sair() {
        token = null
        id = 0
        nome = ""
        tipo = ""
        prefs.edit().clear().apply()
    }

    // rota da tela inicial de cada tipo de usuário
    fun rotaHome(): String = if (isAluno) "home-aluno" else "home-professor"
}
