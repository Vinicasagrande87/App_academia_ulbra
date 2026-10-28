package com.example.app_academia_ulbra

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import com.example.app_academia_ulbra.dados.BancoLocal
import com.example.app_academia_ulbra.dados.CorAviso
import com.example.app_academia_ulbra.dados.FORMAS_PAGAMENTO
import com.example.app_academia_ulbra.dados.Pagamento
import com.example.app_academia_ulbra.dados.PagamentoRequest
import com.example.app_academia_ulbra.dados.PerfilAluno
import com.example.app_academia_ulbra.dados.Plano
import com.example.app_academia_ulbra.dados.SituacaoPlano
import com.example.app_academia_ulbra.dados.Treino
import com.example.app_academia_ulbra.dados.calcularSituacaoPlano
import com.example.app_academia_ulbra.dados.formaPagamentoLabel
import com.example.app_academia_ulbra.dados.formatarData
import com.example.app_academia_ulbra.dados.formatarDataHora
import com.example.app_academia_ulbra.dados.formatarValor
import com.example.app_academia_ulbra.dados.lerErro
import com.example.app_academia_ulbra.dados.lerNumero
import com.example.app_academia_ulbra.dados.mensagemDeErro
import com.example.app_academia_ulbra.dados.ordenarPorDia
import com.example.app_academia_ulbra.ui.theme.Espaco
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TelaAlunoTreinos(irPara: (String) -> Unit) {
    var treinos by remember { mutableStateOf<List<Treino>>(emptyList()) }
    var carregando by remember { mutableStateOf(true) }
    var mensagemPlanoInativo by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            treinos = ordenarPorDia(BancoLocal.meusTreinos())
        } catch (e: Exception) {
            treinos = emptyList()
            val erro = lerErro(e)
            // a API bloqueia os treinos quando o plano venceu ou não foi confirmado
            if (erro.codigo == "PLANO_INATIVO") {
                mensagemPlanoInativo = erro.mensagem
            }
        }
        carregando = false
    }

    ConteudoTela {
        when {
            carregando -> Carregando()

            mensagemPlanoInativo != null -> CardAviso(mensagemPlanoInativo!!, CorAviso.PERIGO) {
                OutlinedButton(
                    onClick = { irPara("aluno-financeiro") },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Espaco.pequeno)
                ) {
                    Text("Ver situação do plano")
                }
            }

            treinos.isEmpty() -> TextoVazio("Você ainda não tem nenhum treino montado. Fala com seu professor!")

            else -> {
                CronometroDescanso()
                treinos.forEach { treino ->
                    CardSimples {
                        Text(treino.diaSemana, style = MaterialTheme.typography.titleLarge)
                        treino.exercicios.forEach { ex ->
                            HorizontalDivider()
                            Text(ex.exercicioNome ?: "", fontWeight = FontWeight.Bold)
                            TextoSecundario("Carga: ${ex.carga ?: "-"}kg • Repetições: ${ex.repeticoes ?: "-"}")
                            ExercicioMidia(ex.videoUrl)
                        }
                    }
                }
            }
        }
    }
}

// contagem de descanso entre as séries; rememberSaveable mantém o tempo ao girar o celular
@Composable
fun CronometroDescanso() {
    val context = LocalContext.current
    var segundos by rememberSaveable { mutableIntStateOf(0) }
    var acabou by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(segundos) {
        if (segundos > 0) {
            delay(1000)
            segundos--
            if (segundos == 0) {
                vibrar(context, 500)
                acabou = true
            }
        }
    }

    CardSimples {
        if (segundos > 0) {
            Text("Descansando: ${segundos}s", style = MaterialTheme.typography.titleLarge)
        }
        Button(
            onClick = {
                acabou = false
                segundos = 60
            },
            enabled = segundos == 0,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Descansar 60s")
        }
    }
    if (acabou) {
        CardAviso("Descanso acabado!", CorAviso.SUCESSO)
    }
}

@Composable
fun TelaAlunoFinanceiro() {
    val context = LocalContext.current
    val escopo = rememberCoroutineScope()

    var planos by remember { mutableStateOf<List<Plano>>(emptyList()) }
    var pagamentos by remember { mutableStateOf<List<Pagamento>>(emptyList()) }
    var situacaoPlano by remember { mutableStateOf<SituacaoPlano?>(null) }
    var planoEscolhido by remember { mutableStateOf<Plano?>(null) }

    suspend fun carregarPagamentos() {
        try {
            pagamentos = BancoLocal.listarPagamentos()
            situacaoPlano = calcularSituacaoPlano(pagamentos)
        } catch (e: Exception) {
            mostrarToast(context, mensagemDeErro(e, "Erro ao carregar pagamentos."))
        }
    }

    LaunchedEffect(Unit) {
        try {
            planos = BancoLocal.listarPlanos()
        } catch (e: Exception) {
            mostrarToast(context, mensagemDeErro(e, "Erro ao carregar planos."))
        }
        carregarPagamentos()
    }

    fun enviarSolicitacao(plano: Plano, formaPagamento: String) {
        escopo.launch {
            try {
                BancoLocal.registrarPagamento(PagamentoRequest(plano.id, formaPagamento))
                mostrarToast(context, "Solicitação enviada! Aguarde a confirmação na recepção.")
                carregarPagamentos()
            } catch (e: Exception) {
                mostrarToast(context, mensagemDeErro(e, "Erro ao enviar solicitação."))
            }
        }
    }

    ConteudoTela {
        situacaoPlano?.let { CardAviso(it.mensagem, it.cor) }

        TituloSecao("Planos disponíveis")
        planos.forEach { plano ->
            CardSimples {
                Text(plano.nome, style = MaterialTheme.typography.titleLarge)
                TextoSecundario("${formatarValor(plano.valor)} • ${plano.duracaoDias ?: "-"} dias")
                plano.descricao?.let { Text(it) }
                Button(onClick = { planoEscolhido = plano }, modifier = Modifier.fillMaxWidth()) {
                    Text("Escolher este plano")
                }
            }
        }

        TituloSecao("Seus pagamentos")
        if (pagamentos.isEmpty()) {
            TextoVazio("Você ainda não tem nenhum pagamento registrado.")
        }
        pagamentos.forEach { pagamento ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text(pagamento.planoNome ?: "", fontWeight = FontWeight.Bold)
                    TextoSecundario("${formatarValor(pagamento.valor)} • ${formaPagamentoLabel(pagamento.formaPagamento)}")
                    if (pagamento.status == "confirmado") {
                        TextoSecundario(
                            "Pago em ${formatarDataHora(pagamento.dataPagamento)} • Válido até ${formatarData(pagamento.validoAte)}"
                        )
                    }
                }
                EtiquetaStatus(pagamento.status == "confirmado")
            }
            HorizontalDivider()
        }
    }

    planoEscolhido?.let { plano ->
        DialogoEscolha(
            titulo = "Plano ${plano.nome}",
            mensagem = "Valor: ${formatarValor(plano.valor)}. Como você vai pagar?",
            opcoes = FORMAS_PAGAMENTO,
            textoConfirmar = "Confirmar escolha",
            aoConfirmar = { forma ->
                planoEscolhido = null
                enviarSolicitacao(plano, forma)
            },
            aoCancelar = { planoEscolhido = null }
        )
    }
}

@Composable
fun TelaAlunoPerfil() {
    val context = LocalContext.current
    val escopo = rememberCoroutineScope()

    var nome by rememberSaveable { mutableStateOf("") }
    var peso by rememberSaveable { mutableStateOf("") }
    var altura by rememberSaveable { mutableStateOf("") }
    var cpf by rememberSaveable { mutableStateOf("") }
    var telefone by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var finalidade by rememberSaveable { mutableStateOf("") }
    var carregando by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val aluno = BancoLocal.meuPerfil()
            nome = aluno.nome
            peso = aluno.peso ?: ""
            altura = aluno.altura ?: ""
            cpf = aluno.cpf ?: ""
            telefone = aluno.telefone ?: ""
            email = aluno.email
            finalidade = aluno.finalidade ?: ""
        } catch (e: Exception) {
            mostrarToast(context, "Erro ao carregar seus dados.")
        }
        carregando = false
    }

    fun salvarPerfil() {
        escopo.launch {
            try {
                val perfil = PerfilAluno(
                    nome = nome.trim(),
                    peso = lerNumero(peso),
                    altura = lerNumero(altura),
                    cpf = cpf.trim(),
                    telefone = telefone.trim(),
                    email = email.trim(),
                    finalidade = finalidade.trim()
                )
                BancoLocal.atualizarPerfil(perfil)
                mostrarToast(context, "Perfil atualizado com sucesso!")
            } catch (e: Exception) {
                mostrarToast(context, mensagemDeErro(e, "Erro ao atualizar perfil."))
            }
        }
    }

    ConteudoTela {
        if (carregando) {
            Carregando()
            return@ConteudoTela
        }
        Column(verticalArrangement = Arrangement.spacedBy(Espaco.pequeno)) {
            CampoTexto(nome, { nome = it }, "Nome completo", "Nome completo")
            CampoTexto(peso, { peso = it }, "Peso (kg)", "Ex: 75,5", KeyboardType.Decimal)
            CampoTexto(altura, { altura = it }, "Altura (m)", "Ex: 1,75", KeyboardType.Decimal)
            CampoTexto(cpf, { cpf = it }, "CPF", "000.000.000-00", KeyboardType.Number)
            CampoTexto(telefone, { telefone = it }, "Telefone", "(00) 00000-0000", KeyboardType.Phone)
            CampoTexto(email, { email = it }, "E-mail", "seu@email.com", KeyboardType.Email)
            CampoTexto(finalidade, { finalidade = it }, "Finalidade", "Ex: Hipertrofia, Emagrecimento")

            Button(
                onClick = { salvarPerfil() },
                enabled = nome.isNotBlank() && email.isNotBlank() && cpf.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Espaco.medio)
            ) {
                Text("SALVAR ALTERAÇÕES")
            }
        }
    }
}
