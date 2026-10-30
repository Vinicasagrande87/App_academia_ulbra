package com.example.app_academia_ulbra

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.app_academia_ulbra.dados.Sessao

// itens da barra de baixo: rota, texto e ícone
private class ItemBarra(val rota: String, val texto: String, val icone: ImageVector)

private val BARRA_ALUNO = listOf(
    ItemBarra("home-aluno", "Início", Icons.Default.Home),
    ItemBarra("aluno-treinos", "Treinos", Icons.Default.FitnessCenter),
    ItemBarra("aluno-financeiro", "Financeiro", Icons.Default.CreditCard),
    ItemBarra("aluno-perfil", "Perfil", Icons.Default.Person)
)

private val BARRA_PROFESSOR = listOf(
    ItemBarra("home-professor", "Início", Icons.Default.Home),
    ItemBarra("alunos-lista", "Alunos", Icons.Default.Group),
    ItemBarra("financeiro", "Financeiro", Icons.Default.CreditCard),
    ItemBarra("anamnese-lista", "Anamnese", Icons.Default.MedicalServices)
)

private val ROTAS_HOME = listOf("home-aluno", "home-professor")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Navegacao() {
    val navController = rememberNavController()
    val entradaAtual by navController.currentBackStackEntryAsState()
    val rotaAtual = entradaAtual?.destination?.route
    val mostrarBarraDeCima = rotaAtual != null && rotaAtual != "inicial"
    val mostrarBarraDeBaixo = rotaAtual != null && rotaAtual !in listOf("inicial", "login")
    val itensBarra = if (Sessao.isAluno) BARRA_ALUNO else BARRA_PROFESSOR

    fun sair() {
        Sessao.sair()
        navController.navigate("login") {
            popUpTo(0) { inclusive = true }
        }
    }

    Scaffold(
        topBar = {
            if (mostrarBarraDeCima) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(R.drawable.rosa_ulbra),
                                contentDescription = null,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(tituloDa(entradaAtual), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    },
                    navigationIcon = {
                        // abas da barra de baixo são telas principais: não mostram a seta de voltar
                        val ehAba = itensBarra.any { it.rota == rotaAtual }
                        if (!ehAba && rotaAtual != "login" && navController.previousBackStackEntry != null) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                            }
                        }
                    },
                    actions = {
                        if (rotaAtual in ROTAS_HOME) {
                            TextButton(onClick = { sair() }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Sair", color = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        },
        bottomBar = {
            if (mostrarBarraDeBaixo) {
                NavigationBar {
                    itensBarra.forEach { item ->
                        NavigationBarItem(
                            selected = rotaAtual == item.rota,
                            onClick = { irPelaBarra(navController, item.rota) },
                            icon = { Icon(item.icone, contentDescription = item.texto) },
                            label = { Text(item.texto) }
                        )
                    }
                }
            }
        },
        contentWindowInsets = if (mostrarBarraDeCima) ScaffoldDefaults.contentWindowInsets else WindowInsets(0)
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "inicial",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("inicial") {
                TelaInicial(aoTerminar = {
                    // se já estava logado, vai direto pra área dele
                    val destino = if (Sessao.logado) Sessao.rotaHome() else "login"
                    navController.navigate(destino) {
                        popUpTo("inicial") { inclusive = true }
                    }
                })
            }
            composable("login") {
                TelaLogin(aoEntrar = {
                    navController.navigate(Sessao.rotaHome()) {
                        popUpTo("login") { inclusive = true }
                    }
                })
            }

            // ----- aluno -----
            composable("home-aluno") {
                TelaHomeAluno(irPara = { navController.navigate(it) })
            }
            composable("aluno-treinos") {
                TelaAlunoTreinos(irPara = { irPelaBarra(navController, it) })
            }
            composable("aluno-financeiro") { TelaAlunoFinanceiro() }
            composable("aluno-perfil") { TelaAlunoPerfil() }

            // ----- professor / admin -----
            composable("home-professor") {
                TelaHomeProfessor(irPara = { navController.navigate(it) })
            }
            composable("aluno-cadastro") {
                TelaAlunoCadastro(aoSalvar = { navController.popBackStack() })
            }
            composable("alunos-lista") {
                TelaAlunosLista(abrirFicha = { aluno ->
                    navController.navigate("aluno-ficha/${aluno.id}?nome=${Uri.encode(aluno.nome)}")
                })
            }
            composable(
                "aluno-ficha/{id}?nome={nome}",
                arguments = listOf(
                    navArgument("id") { type = NavType.IntType },
                    navArgument("nome") { type = NavType.StringType; defaultValue = "" }
                )
            ) { entrada ->
                val alunoId = entrada.arguments?.getInt("id") ?: 0
                TelaAlunoFicha(
                    alunoId = alunoId,
                    montarTreino = { navController.navigate("treino-cadastro/$alunoId") },
                    editarDia = { dia -> navController.navigate("treino-editar/$alunoId/${Uri.encode(dia)}") }
                )
            }
            composable(
                "treino-cadastro/{alunoId}",
                arguments = listOf(navArgument("alunoId") { type = NavType.IntType })
            ) { entrada ->
                TelaTreinoCadastro(
                    alunoId = entrada.arguments?.getInt("alunoId") ?: 0,
                    aoSalvar = { navController.popBackStack() }
                )
            }
            composable(
                "treino-editar/{alunoId}/{dia}",
                arguments = listOf(
                    navArgument("alunoId") { type = NavType.IntType },
                    navArgument("dia") { type = NavType.StringType }
                )
            ) { entrada ->
                TelaTreinoEditar(
                    alunoId = entrada.arguments?.getInt("alunoId") ?: 0,
                    dia = entrada.arguments?.getString("dia") ?: "",
                    aoSalvar = { navController.popBackStack() }
                )
            }
            composable("financeiro") { TelaFinanceiro() }
            composable("anamnese-lista") {
                TelaAnamneseLista(abrirAnamnese = { aluno ->
                    navController.navigate("aluno-anamnese/${aluno.id}?nome=${Uri.encode(aluno.nome)}")
                })
            }
            composable(
                "aluno-anamnese/{alunoId}?nome={nome}",
                arguments = listOf(
                    navArgument("alunoId") { type = NavType.IntType },
                    navArgument("nome") { type = NavType.StringType; defaultValue = "" }
                )
            ) { entrada ->
                TelaAlunoAnamnese(
                    alunoId = entrada.arguments?.getInt("alunoId") ?: 0,
                    aoSalvar = { navController.popBackStack() }
                )
            }

            // ----- só admin -----
            composable(
                "exercicio-cadastro?grupo={grupo}&exercicioId={exercicioId}",
                arguments = listOf(
                    navArgument("grupo") { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument("exercicioId") { type = NavType.IntType; defaultValue = 0 }
                )
            ) { entrada ->
                TelaExercicioCadastro(
                    grupoInicial = entrada.arguments?.getString("grupo"),
                    exercicioIdInicial = entrada.arguments?.getInt("exercicioId")?.takeIf { it != 0 },
                    verTodos = { navController.navigate("exercicios-lista") },
                    aoSalvar = { navController.popBackStack("home-professor", inclusive = false) }
                )
            }
            composable("exercicios-lista") {
                TelaExerciciosLista(editar = { ex ->
                    navController.navigate("exercicio-cadastro?grupo=${Uri.encode(ex.grupoMuscular)}&exercicioId=${ex.id}")
                })
            }
            composable("professor-cadastro") {
                TelaProfessorCadastro(aoSalvar = { navController.popBackStack() })
            }
            composable("professores-lista") {
                TelaProfessoresLista(cadastrarNovo = { navController.navigate("professor-cadastro") })
            }
            composable("planos-gerenciar") { TelaPlanosGerenciar() }
        }
    }
}

private fun tituloDa(entrada: NavBackStackEntry?): String {
    val rota = entrada?.destination?.route ?: return "Ulbra Gym"
    val nomeAluno = entrada.arguments?.getString("nome").orEmpty()
    return when {
        rota == "login" -> "Login"
        rota == "home-aluno" -> "Área do Aluno"
        rota == "home-professor" -> if (Sessao.isAdmin) "Painel do Admin" else "Painel do Professor"
        rota == "aluno-treinos" -> "Meus Treinos"
        rota == "aluno-financeiro" -> "Planos e Pagamentos"
        rota == "aluno-perfil" -> "Meu Perfil"
        rota == "aluno-cadastro" -> "Cadastrar Aluno"
        rota == "alunos-lista" -> "Alunos Matriculados"
        rota.startsWith("aluno-ficha") -> nomeAluno.ifBlank { "Ficha de Treino" }
        rota.startsWith("treino-cadastro") -> "Montar Treino"
        rota.startsWith("treino-editar") -> "Editar ${entrada.arguments?.getString("dia").orEmpty()}"
        rota == "financeiro" -> "Planos e Pagamentos"
        rota == "anamnese-lista" -> "Anamnese"
        rota.startsWith("aluno-anamnese") -> if (nomeAluno.isBlank()) "Anamnese" else "Anamnese - $nomeAluno"
        rota.startsWith("exercicio-cadastro") -> "Cadastrar Exercício"
        rota == "exercicios-lista" -> "Exercícios Cadastrados"
        rota == "professor-cadastro" -> "Cadastrar Professor"
        rota == "professores-lista" -> "Professores Cadastrados"
        rota == "planos-gerenciar" -> "Gerenciar Planos"
        else -> "Ulbra Gym"
    }
}

// navega pela barra de baixo sem empilhar a mesma tela várias vezes
private fun irPelaBarra(navController: NavHostController, rota: String) {
    navController.navigate(rota) {
        popUpTo(Sessao.rotaHome())
        launchSingleTop = true
    }
}
