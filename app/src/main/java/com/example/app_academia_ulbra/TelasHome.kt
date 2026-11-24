package com.example.app_academia_ulbra

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Pool
import androidx.compose.material.icons.outlined.SportsGymnastics
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.example.app_academia_ulbra.dados.BancoLocal
import com.example.app_academia_ulbra.dados.CorAviso
import com.example.app_academia_ulbra.dados.Sessao
import com.example.app_academia_ulbra.dados.SituacaoPlano
import com.example.app_academia_ulbra.dados.calcularSituacaoPlano
import com.example.app_academia_ulbra.ui.theme.AmareloAtencao
import com.example.app_academia_ulbra.ui.theme.VerdeSucesso
import com.example.app_academia_ulbra.ui.theme.VermelhoPerigo

@Composable
private fun DialogoPiscina(mensagem: String, aoFechar: () -> Unit) {
    AlertDialog(
        onDismissRequest = aoFechar,
        title = { Text("Piscina Olímpica") },
        text = { Text(mensagem) },
        confirmButton = { TextButton(onClick = aoFechar) { Text("Entendi") } }
    )
}

@Composable
fun TelaHomeAluno(irPara: (String) -> Unit) {
    var avisoPlano by remember { mutableStateOf<SituacaoPlano?>(null) }
    var mostrarPiscina by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val situacao = calcularSituacaoPlano(BancoLocal.listarPagamentos())
            // só mostra o aviso quando tem algo pra avisar (vencendo, vencido ou sem plano)
            avisoPlano = if (situacao.cor != CorAviso.SUCESSO) situacao else null
        } catch (e: Exception) {
            avisoPlano = null
        }
    }

    ConteudoTela {
        BannerBoasVindas(
            "Olá, ${Sessao.nome.ifBlank { "Aluno(a)" }}!",
            "Acompanhe seus treinos e sua situação financeira."
        )

        avisoPlano?.let {
            CardAviso(it.mensagem, it.cor, aoClicar = { irPara("aluno-financeiro") })
        }

        CardMenu(Icons.Outlined.FitnessCenter, MaterialTheme.colorScheme.primary, "Meus Treinos", "Visualizar suas fichas de treino atuais") {
            irPara("aluno-treinos")
        }
        CardMenu(Icons.Outlined.CreditCard, VerdeSucesso, "Planos e Pagamentos", "Consultar status da mensalidade e plano ativo") {
            irPara("aluno-financeiro")
        }
        CardMenu(Icons.Outlined.Person, AmareloAtencao, "Meu Perfil", "Atualizar dados cadastrais") {
            irPara("aluno-perfil")
        }
        CardMenu(
            Icons.Outlined.Pool, MaterialTheme.colorScheme.secondary, "Piscina Olímpica",
            "Natação e hidroginástica — em breve por aqui", novidade = true
        ) {
            mostrarPiscina = true
        }
    }

    if (mostrarPiscina) {
        DialogoPiscina(
            "Em breve você vai poder se inscrever direto por aqui nas aulas de natação (adulto e infantil) e hidroginástica. Fique de olho nas novidades!"
        ) { mostrarPiscina = false }
    }
}

// a mesma tela serve pro professor e pro admin; o admin vê também a parte de Administração
@Composable
fun TelaHomeProfessor(irPara: (String) -> Unit) {
    var mostrarPiscina by remember { mutableStateOf(false) }
    val verde = MaterialTheme.colorScheme.primary

    ConteudoTela {
        BannerBoasVindas(
            if (Sessao.isAdmin) "Bem-vindo, Gestor!" else "Bem-vindo, Prof. ${Sessao.nome}!",
            "Selecione abaixo a opção desejada para gerenciar a academia."
        )

        CardMenu(Icons.Outlined.PersonAdd, verde, "Cadastrar Aluno", "Registrar novos alunos no sistema") {
            irPara("aluno-cadastro")
        }
        CardMenu(Icons.Outlined.Group, verde, "Alunos Cadastrados", "Visualizar e gerenciar alunos, montar e editar treinos") {
            irPara("alunos-lista")
        }
        CardMenu(Icons.Outlined.CreditCard, VerdeSucesso, "Planos e Pagamentos", "Controle financeiro e mensalidades") {
            irPara("financeiro")
        }
        CardMenu(Icons.Outlined.MedicalServices, VermelhoPerigo, "Anamnese", "Ficha de saúde e contato de emergência dos alunos") {
            irPara("anamnese-lista")
        }
        CardMenu(
            Icons.Outlined.Pool, MaterialTheme.colorScheme.secondary, "Piscina Olímpica",
            "Natação e hidroginástica — módulo em construção", novidade = true
        ) {
            mostrarPiscina = true
        }

        if (Sessao.isAdmin) {
            BannerBoasVindas("Administração", "Gerenciamento de professores, exercícios e da vitrine de planos.")

            CardMenu(Icons.Outlined.SportsGymnastics, Color(0xFF5260FF), "Cadastrar Exercícios", "Adicionar exercícios e vídeos explicativos") {
                irPara("exercicio-cadastro")
            }
            CardMenu(Icons.Outlined.PersonAdd, AmareloAtencao, "Cadastrar Professor", "Registrar novos professores no sistema") {
                irPara("professor-cadastro")
            }
            CardMenu(Icons.Outlined.Group, MaterialTheme.colorScheme.secondary, "Professores Cadastrados", "Editar ou remover professores") {
                irPara("professores-lista")
            }
            CardMenu(Icons.Outlined.LocalOffer, VermelhoPerigo, "Gerenciar Planos", "Criar, editar ou remover planos da vitrine") {
                irPara("planos-gerenciar")
            }
        }
    }

    if (mostrarPiscina) {
        DialogoPiscina(
            "Novo espaço da academia: natação (adulto e infantil) e hidroginástica. O módulo de turmas e matrícula ainda está sendo construído."
        ) { mostrarPiscina = false }
    }
}
