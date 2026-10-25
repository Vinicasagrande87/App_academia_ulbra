package com.example.app_academia_ulbra

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.app_academia_ulbra.dados.Aluno
import com.example.app_academia_ulbra.dados.Anamnese
import com.example.app_academia_ulbra.dados.BancoLocal
import com.example.app_academia_ulbra.dados.ConfirmacaoPagamento
import com.example.app_academia_ulbra.dados.CorAviso
import com.example.app_academia_ulbra.dados.DIAS_DA_SEMANA
import com.example.app_academia_ulbra.dados.Exercicio
import com.example.app_academia_ulbra.dados.FORMAS_PAGAMENTO
import com.example.app_academia_ulbra.dados.GRUPOS_MUSCULARES
import com.example.app_academia_ulbra.dados.ItemTreinoRequest
import com.example.app_academia_ulbra.dados.NovoAluno
import com.example.app_academia_ulbra.dados.Pagamento
import com.example.app_academia_ulbra.dados.PagamentoRequest
import com.example.app_academia_ulbra.dados.Plano
import com.example.app_academia_ulbra.dados.SituacaoPlano
import com.example.app_academia_ulbra.dados.Treino
import com.example.app_academia_ulbra.dados.TreinoRequest
import com.example.app_academia_ulbra.dados.calcularSituacaoPlano
import com.example.app_academia_ulbra.dados.formaPagamentoLabel
import com.example.app_academia_ulbra.dados.formatarDataHora
import com.example.app_academia_ulbra.dados.formatarValor
import com.example.app_academia_ulbra.dados.lerNumero
import com.example.app_academia_ulbra.dados.mensagemDeErro
import com.example.app_academia_ulbra.dados.ordenarPorDia
import com.example.app_academia_ulbra.ui.theme.Espaco
import com.example.app_academia_ulbra.ui.theme.VerdeSucesso
import com.example.app_academia_ulbra.ui.theme.VermelhoPerigo
import kotlinx.coroutines.launch

// ---------- Cadastrar aluno ----------

@Composable
fun TelaAlunoCadastro(aoSalvar: () -> Unit) {
    val context = LocalContext.current
    val escopo = rememberCoroutineScope()

    var nome by rememberSaveable { mutableStateOf("") }
    var idade by rememberSaveable { mutableStateOf("") }
    var peso by rememberSaveable { mutableStateOf("") }
    var altura by rememberSaveable { mutableStateOf("") }
    var cpf by rememberSaveable { mutableStateOf("") }
    var telefone by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var senha by rememberSaveable { mutableStateOf("") }
    var finalidade by rememberSaveable { mutableStateOf("") }
    var tentouSalvar by rememberSaveable { mutableStateOf(false) }
    var salvando by remember { mutableStateOf(false) }

    // mesmas validações do formulário do site
    fun obrigatorio(valor: String) = if (tentouSalvar && valor.isBlank()) "Campo obrigatório" else null

    val pesoNumero = lerNumero(peso)
    val alturaNumero = lerNumero(altura)
    val erroPeso = when {
        peso.isBlank() -> obrigatorio(peso)
        pesoNumero == null || pesoNumero < 1 || pesoNumero > 400 -> "Peso inválido"
        else -> null
    }
    val erroAltura = when {
        altura.isBlank() -> obrigatorio(altura)
        alturaNumero == null || alturaNumero < 0.5 || alturaNumero > 2.5 -> "Digite em metros, ex: 1.75 (não em centímetros)"
        else -> null
    }
    val erroSenha = when {
        senha.isBlank() -> obrigatorio(senha)
        senha.length < 6 -> "Mínimo de 6 caracteres"
        else -> null
    }

    val formularioValido = listOf(nome, idade, cpf, telefone, email, finalidade).all { it.isNotBlank() } &&
        idade.toIntOrNull() != null && pesoNumero != null && erroPeso == null &&
        alturaNumero != null && erroAltura == null && senha.length >= 6

    fun cadastrar() {
        tentouSalvar = true
        if (!formularioValido || salvando) return
        salvando = true
        escopo.launch {
            try {
                BancoLocal.cadastrarAluno(
                    NovoAluno(
                        nome = nome.trim(),
                        idade = idade.toIntOrNull(),
                        peso = pesoNumero,
                        altura = alturaNumero,
                        cpf = cpf.trim(),
                        telefone = telefone.trim(),
                        email = email.trim(),
                        senha = senha,
                        finalidade = finalidade.trim()
                    )
                )
                mostrarToast(context, "Aluno cadastrado com sucesso!")
                aoSalvar()
            } catch (e: Exception) {
                mostrarToast(context, mensagemDeErro(e, "Erro ao cadastrar aluno."))
            } finally {
                salvando = false
            }
        }
    }

    ConteudoTela {
        CampoTexto(nome, { nome = it }, "Nome completo", "Nome completo", erro = obrigatorio(nome))
        CampoTexto(idade, { idade = it.filter(Char::isDigit) }, "Idade", "Ex: 28", KeyboardType.Number, erro = obrigatorio(idade))
        CampoTexto(peso, { peso = it }, "Peso (kg)", "Ex: 75.5", KeyboardType.Decimal, erro = erroPeso)
        CampoTexto(altura, { altura = it }, "Altura (m)", "Ex: 1.75 (em metros)", KeyboardType.Decimal, erro = erroAltura)
        CampoTexto(cpf, { cpf = it }, "CPF", "000.000.000-00", KeyboardType.Number, erro = obrigatorio(cpf))
        CampoTexto(telefone, { telefone = it }, "Telefone", "(00) 00000-0000", KeyboardType.Phone, erro = obrigatorio(telefone))
        CampoTexto(email, { email = it }, "E-mail", "aluno@email.com", KeyboardType.Email, erro = obrigatorio(email))
        CampoTexto(senha, { senha = it }, "Senha", "Senha inicial do aluno", KeyboardType.Password, senha = true, erro = erroSenha)
        CampoTexto(finalidade, { finalidade = it }, "Finalidade", "Ex: Hipertrofia, Emagrecimento", erro = obrigatorio(finalidade))

        Button(
            onClick = { cadastrar() },
            enabled = !salvando,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Espaco.medio)
        ) {
            Text(if (salvando) "Salvando..." else "SALVAR ALUNO")
        }
    }
}

// ---------- Lista de alunos (com busca e "Vincular plano") ----------

@Composable
fun CampoBusca(filtro: String, aoMudar: (String) -> Unit, placeholder: String) {
    OutlinedTextField(
        value = filtro,
        onValueChange = aoMudar,
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ItemAluno(aluno: Aluno, aoClicar: () -> Unit, iconeInicio: Boolean = false, acao: @Composable () -> Unit = {}) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = aoClicar)
            .padding(vertical = Espaco.pequeno)
    ) {
        if (iconeInicio) {
            Icon(Icons.Outlined.MedicalServices, contentDescription = null, tint = VermelhoPerigo)
            Spacer(Modifier.width(Espaco.medio))
        }
        Column(Modifier.weight(1f)) {
            Text(aluno.nome, fontWeight = FontWeight.Bold)
            TextoSecundario(aluno.email)
            if (!aluno.finalidade.isNullOrBlank() && !iconeInicio) TextoSecundario(aluno.finalidade)
        }
        acao()
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
    }
    HorizontalDivider()
}

private fun filtrarAlunos(alunos: List<Aluno>, filtro: String): List<Aluno> {
    val termo = filtro.trim().lowercase()
    if (termo.isEmpty()) return alunos
    return alunos.filter { it.nome.lowercase().contains(termo) }
}

@Composable
fun TelaAlunosLista(abrirFicha: (Aluno) -> Unit) {
    val context = LocalContext.current
    val escopo = rememberCoroutineScope()

    var alunos by remember { mutableStateOf<List<Aluno>>(emptyList()) }
    var planos by remember { mutableStateOf<List<Plano>>(emptyList()) }
    var filtro by rememberSaveable { mutableStateOf("") }

    // "Vincular plano" é um passo a passo: plano -> forma de pagamento -> valor
    var alunoVinculando by remember { mutableStateOf<Aluno?>(null) }
    var planoEscolhido by remember { mutableStateOf<Plano?>(null) }
    var formaEscolhida by remember { mutableStateOf<String?>(null) }
    var valorPago by remember { mutableStateOf("") }
    var observacao by remember { mutableStateOf("") }

    fun cancelarVinculo() {
        alunoVinculando = null
        planoEscolhido = null
        formaEscolhida = null
    }

    LaunchedEffect(Unit) {
        try {
            alunos = BancoLocal.listarAlunos()
        } catch (e: Exception) {
            mostrarToast(context, mensagemDeErro(e, "Erro ao carregar alunos."))
        }
        try {
            planos = BancoLocal.listarPlanos()
        } catch (e: Exception) {
            planos = emptyList()
        }
    }

    fun registrarPagamento(aluno: Aluno, plano: Plano, forma: String) {
        val valor = lerNumero(valorPago)
        if (valor == null || valor <= 0) {
            mostrarToast(context, "Informe um valor válido para o pagamento.")
            return
        }
        cancelarVinculo()
        escopo.launch {
            try {
                BancoLocal.registrarPagamento(
                    PagamentoRequest(
                        planoId = plano.id,
                        formaPagamento = forma,
                        alunoId = aluno.id,
                        valor = valor,
                        observacao = observacao.ifBlank { null }
                    )
                )
                mostrarToast(context, "Plano vinculado a ${aluno.nome} com sucesso! Já pode montar o treino.")
            } catch (e: Exception) {
                mostrarToast(context, mensagemDeErro(e, "Erro ao vincular plano."))
            }
        }
    }

    val filtrados = filtrarAlunos(alunos, filtro)

    ConteudoTela {
        CampoBusca(filtro, { filtro = it }, "Buscar aluno pelo nome")

        when {
            alunos.isEmpty() -> TextoVazio("Nenhum aluno cadastrado ainda.")
            filtrados.isEmpty() -> TextoVazio("Nenhum aluno encontrado com esse nome.")
        }

        filtrados.forEach { aluno ->
            ItemAluno(aluno, aoClicar = { abrirFicha(aluno) }) {
                OutlinedButton(onClick = {
                    if (planos.isEmpty()) {
                        mostrarToast(context, "Nenhum plano cadastrado ainda. Cadastre um plano primeiro em \"Gerenciar Planos\".")
                    } else {
                        alunoVinculando = aluno
                    }
                }) {
                    Text("Vincular plano")
                }
            }
        }
    }

    val aluno = alunoVinculando ?: return
    val plano = planoEscolhido
    val forma = formaEscolhida

    when {
        plano == null -> DialogoEscolha(
            titulo = "Vincular plano — ${aluno.nome}",
            opcoes = planos.map { it to "${it.nome} — ${formatarValor(it.valor)} (${it.duracaoDias ?: "-"} dias)" },
            textoConfirmar = "Próximo",
            aoConfirmar = { planoEscolhido = it },
            aoCancelar = { cancelarVinculo() }
        )

        forma == null -> DialogoEscolha(
            titulo = "Forma de pagamento",
            opcoes = FORMAS_PAGAMENTO,
            textoConfirmar = "Próximo",
            aoConfirmar = {
                formaEscolhida = it
                valorPago = plano.valor ?: ""
                observacao = ""
            },
            aoCancelar = { cancelarVinculo() }
        )

        else -> DialogoFormulario(
            titulo = "Confirmar valor pago",
            textoConfirmar = "Confirmar pagamento",
            aoConfirmar = { registrarPagamento(aluno, plano, forma) },
            aoCancelar = { cancelarVinculo() }
        ) {
            CampoTexto(valorPago, { valorPago = it }, "Valor pago", teclado = KeyboardType.Decimal)
            CampoTexto(observacao, { observacao = it }, "Observação (opcional)")
        }
    }
}

// ---------- Ficha do aluno (treinos montados) ----------

@Composable
fun TelaAlunoFicha(
    alunoId: Int,
    montarTreino: () -> Unit,
    editarDia: (String) -> Unit
) {
    val context = LocalContext.current
    val escopo = rememberCoroutineScope()

    var treinos by remember { mutableStateOf<List<Treino>>(emptyList()) }
    var situacaoPlano by remember { mutableStateOf<SituacaoPlano?>(null) }
    var treinoExcluindo by remember { mutableStateOf<Treino?>(null) }

    LaunchedEffect(Unit) {
        try {
            treinos = ordenarPorDia(BancoLocal.treinosDoAluno(alunoId))
        } catch (e: Exception) {
            mostrarToast(context, mensagemDeErro(e, "Erro ao carregar treinos do aluno."))
        }
        try {
            situacaoPlano = calcularSituacaoPlano(BancoLocal.listarPagamentos(alunoId = alunoId))
        } catch (e: Exception) {
            situacaoPlano = null
        }
    }

    // sem plano ativo não dá pra montar treino novo (editar continua liberado)
    val planoBloqueado = situacaoPlano?.cor == CorAviso.PERIGO || situacaoPlano?.cor == CorAviso.NEUTRO

    fun excluir(treino: Treino) {
        treinoExcluindo = null
        escopo.launch {
            try {
                BancoLocal.excluirTreino(treino.id)
                treinos = treinos.filter { it.id != treino.id }
                mostrarToast(context, "Treino excluído com sucesso!")
            } catch (e: Exception) {
                mostrarToast(context, mensagemDeErro(e, "Erro ao excluir treino."))
            }
        }
    }

    ConteudoTela {
        situacaoPlano?.let {
            val complemento = if (planoBloqueado) {
                " Confirme o pagamento na tela financeira antes de montar um treino novo pra esse aluno."
            } else ""
            CardAviso(it.mensagem + complemento, it.cor)
        }

        Button(onClick = montarTreino, enabled = !planoBloqueado, modifier = Modifier.fillMaxWidth()) {
            Text("+ Montar novo treino")
        }

        if (treinos.isEmpty()) {
            TextoVazio("Esse aluno ainda não tem nenhum treino montado.")
        }

        treinos.forEach { treino ->
            CardSimples {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(treino.diaSemana, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    OutlinedButton(onClick = { editarDia(treino.diaSemana) }) { Text("Editar Dia") }
                    Spacer(Modifier.width(4.dp))
                    OutlinedButton(
                        onClick = { treinoExcluindo = treino },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = VermelhoPerigo)
                    ) { Text("Excluir") }
                }
                if (treino.exercicios.isEmpty()) {
                    Text("Nenhum exercício vinculado a esse treino ainda.")
                }
                treino.exercicios.forEach { ex ->
                    HorizontalDivider()
                    Text(ex.exercicioNome ?: "", fontWeight = FontWeight.Bold)
                    TextoSecundario("Carga: ${ex.carga ?: "-"}kg • Repetições: ${ex.repeticoes ?: "-"}")
                    ExercicioMidia(ex.videoUrl)
                }
            }
        }
    }

    treinoExcluindo?.let { treino ->
        DialogoConfirmacao(
            titulo = "Excluir treino",
            mensagem = "Tem certeza que deseja excluir o treino de ${treino.diaSemana}? Essa ação não pode ser desfeita.",
            textoConfirmar = "Excluir",
            perigo = true,
            aoConfirmar = { excluir(treino) },
            aoCancelar = { treinoExcluindo = null }
        )
    }
}

// ---------- Montar / editar treino ----------

// cada exercício do formulário de treino
class ItemFormulario(grupo: String = "", exercicioId: Int? = null, carga: String = "", repeticoes: String = "") {
    var grupo by mutableStateOf(grupo)
    var exercicioId by mutableStateOf(exercicioId)
    var carga by mutableStateOf(carga)
    var repeticoes by mutableStateOf(repeticoes)
}

// a carga vai pra API só com os dígitos, igual o site faz ("40kg" -> 40)
private fun montarItens(itens: List<ItemFormulario>) = itens.mapIndexed { i, item ->
    ItemTreinoRequest(
        exercicioId = item.exercicioId ?: 0,
        carga = item.carga.filter(Char::isDigit).toIntOrNull(),
        repeticoes = item.repeticoes.trim(),
        ordem = i + 1
    )
}

@Composable
private fun FormularioItensTreino(itens: MutableList<ItemFormulario>, catalogo: List<Exercicio>, mostrarMidia: Boolean) {
    itens.forEachIndexed { indice, item ->
        CardSimples {
            Seletor(
                rotulo = "Grupo Muscular",
                opcoes = GRUPOS_MUSCULARES.map { it to it },
                selecionado = item.grupo.ifBlank { null },
                aoSelecionar = {
                    item.grupo = it
                    item.exercicioId = null
                },
                placeholder = "Selecione o grupo"
            )
            Seletor(
                rotulo = "Exercício",
                opcoes = catalogo.filter { it.grupoMuscular == item.grupo }.map { it.id to it.nome },
                selecionado = item.exercicioId,
                aoSelecionar = { item.exercicioId = it },
                placeholder = "Selecione o exercício",
                habilitado = item.grupo.isNotBlank()
            )
            if (mostrarMidia) {
                catalogo.firstOrNull { it.id == item.exercicioId }?.let {
                    ExercicioMidia(it.videoUrl)
                }
            }
            CampoTexto(item.carga, { item.carga = it }, "Carga (kg)", "Ex: 40")
            CampoTexto(item.repeticoes, { item.repeticoes = it }, "Repetições", "Ex: 3x12")
            if (itens.size > 1) {
                TextButton(onClick = { itens.removeAt(indice) }) {
                    Text("Remover exercício", color = VermelhoPerigo)
                }
            }
        }
    }
}

private fun itensValidos(itens: List<ItemFormulario>) = itens.isNotEmpty() && itens.all {
    it.grupo.isNotBlank() && it.exercicioId != null && it.carga.isNotBlank() && it.repeticoes.isNotBlank()
}

@Composable
fun TelaTreinoCadastro(alunoId: Int, aoSalvar: () -> Unit) {
    val context = LocalContext.current
    val escopo = rememberCoroutineScope()

    var catalogo by remember { mutableStateOf<List<Exercicio>>(emptyList()) }
    var diaSemana by rememberSaveable { mutableStateOf("") }
    val itens = remember { mutableStateListOf(ItemFormulario()) }
    var salvando by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            catalogo = BancoLocal.listarExercicios()
        } catch (e: Exception) {
            mostrarToast(context, mensagemDeErro(e, "Erro ao carregar exercícios."))
        }
    }

    fun salvar() {
        if (salvando) return
        salvando = true
        escopo.launch {
            try {
                BancoLocal.criarTreino(TreinoRequest(alunoId, diaSemana, montarItens(itens)))
                mostrarToast(context, "Treino criado com sucesso!")
                aoSalvar()
            } catch (e: Exception) {
                mostrarToast(context, mensagemDeErro(e, "Erro ao montar treino."))
            } finally {
                salvando = false
            }
        }
    }

    ConteudoTela {
        Seletor(
            rotulo = "Dia da semana",
            opcoes = DIAS_DA_SEMANA.map { it to it },
            selecionado = diaSemana.ifBlank { null },
            aoSelecionar = { diaSemana = it },
            placeholder = "Selecione o dia"
        )

        TituloSecao("Exercícios do treino")
        FormularioItensTreino(itens, catalogo, mostrarMidia = true)

        OutlinedButton(onClick = { itens.add(ItemFormulario()) }, modifier = Modifier.fillMaxWidth()) {
            Text("+ Adicionar exercício")
        }
        Button(
            onClick = { salvar() },
            enabled = !salvando && diaSemana.isNotBlank() && itensValidos(itens),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("SALVAR TREINO")
        }
    }
}

@Composable
fun TelaTreinoEditar(alunoId: Int, dia: String, aoSalvar: () -> Unit) {
    val context = LocalContext.current
    val escopo = rememberCoroutineScope()

    var catalogo by remember { mutableStateOf<List<Exercicio>>(emptyList()) }
    var treinoId by remember { mutableStateOf<Int?>(null) }
    val itens = remember { mutableStateListOf<ItemFormulario>() }
    var carregando by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            catalogo = BancoLocal.listarExercicios()
            val treinoDoDia = BancoLocal.treinosDoAluno(alunoId).firstOrNull { it.diaSemana == dia }
            if (treinoDoDia == null) {
                mostrarToast(context, "Nenhum treino encontrado para esse dia.")
            } else {
                treinoId = treinoDoDia.id
                itens.clear()
                treinoDoDia.exercicios.sortedBy { it.ordem ?: 0 }.forEach { item ->
                    // o grupo não vem no treino, então busca no catálogo
                    val noCatalogo = catalogo.firstOrNull { it.id == item.exercicioId }
                    itens.add(
                        ItemFormulario(
                            grupo = noCatalogo?.grupoMuscular ?: "",
                            exercicioId = item.exercicioId,
                            carga = item.carga ?: "",
                            repeticoes = item.repeticoes ?: ""
                        )
                    )
                }
            }
        } catch (e: Exception) {
            mostrarToast(context, mensagemDeErro(e, "Erro ao carregar treino."))
        }
        carregando = false
    }

    fun salvar() {
        val id = treinoId
        if (!itensValidos(itens)) {
            mostrarToast(context, "Preencha grupo, exercício, carga e repetições de todos os itens.")
            return
        }
        if (id == null) {
            mostrarToast(context, "Não foi possível identificar o treino a ser editado.")
            return
        }
        escopo.launch {
            try {
                BancoLocal.editarTreino(id, TreinoRequest(null, dia, montarItens(itens)))
                mostrarToast(context, "Treino atualizado com sucesso!")
                aoSalvar()
            } catch (e: Exception) {
                mostrarToast(context, mensagemDeErro(e, "Erro ao salvar alterações."))
            }
        }
    }

    ConteudoTela {
        if (carregando) {
            Carregando()
            return@ConteudoTela
        }
        FormularioItensTreino(itens, catalogo, mostrarMidia = false)

        Button(onClick = { itens.add(ItemFormulario()) }, modifier = Modifier.fillMaxWidth()) {
            Text("+ Adicionar Exercício")
        }
        Button(
            onClick = { salvar() },
            enabled = itensValidos(itens),
            colors = ButtonDefaults.buttonColors(containerColor = VerdeSucesso),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("SALVAR ALTERAÇÕES")
        }
    }
}

// ---------- Financeiro (professor/admin) ----------

@Composable
fun TelaFinanceiro() {
    val context = LocalContext.current
    val escopo = rememberCoroutineScope()

    var planos by remember { mutableStateOf<List<Plano>>(emptyList()) }
    var pagamentos by remember { mutableStateOf<List<Pagamento>>(emptyList()) }
    // "pendente" ou "" (todos), igual ao segment do site
    var filtroStatus by rememberSaveable { mutableStateOf("pendente") }
    var confirmando by remember { mutableStateOf<Pagamento?>(null) }
    var observacao by remember { mutableStateOf("") }
    var recarregar by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        try {
            planos = BancoLocal.listarPlanos()
        } catch (e: Exception) {
            mostrarToast(context, mensagemDeErro(e, "Erro ao carregar planos."))
        }
    }

    LaunchedEffect(filtroStatus, recarregar) {
        try {
            pagamentos = BancoLocal.listarPagamentos(status = filtroStatus.ifBlank { null })
        } catch (e: Exception) {
            mostrarToast(context, mensagemDeErro(e, "Erro ao carregar pagamentos."))
        }
    }

    fun confirmar(pagamento: Pagamento) {
        confirmando = null
        escopo.launch {
            try {
                BancoLocal.confirmarPagamento(
                    pagamento.id,
                    ConfirmacaoPagamento("confirmado", pagamento.valor, pagamento.formaPagamento, observacao)
                )
                mostrarToast(context, "Pagamento confirmado!")
                recarregar++
            } catch (e: Exception) {
                mostrarToast(context, mensagemDeErro(e, "Erro ao confirmar pagamento."))
            }
        }
    }

    ConteudoTela {
        TituloSecao("Planos cadastrados")
        planos.forEach { plano ->
            CardSimples {
                Text(plano.nome, style = MaterialTheme.typography.titleLarge)
                TextoSecundario("${formatarValor(plano.valor)} • ${plano.duracaoDias ?: "-"} dias")
                plano.descricao?.let { Text(it) }
            }
        }

        TituloSecao("Pagamentos")
        val opcoesFiltro = listOf("pendente" to "Pendentes", "" to "Todos")
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            opcoesFiltro.forEachIndexed { i, (valor, texto) ->
                SegmentedButton(
                    selected = filtroStatus == valor,
                    onClick = { filtroStatus = valor },
                    shape = SegmentedButtonDefaults.itemShape(i, opcoesFiltro.size)
                ) { Text(texto) }
            }
        }

        if (pagamentos.isEmpty()) {
            TextoVazio("Nenhum pagamento encontrado.")
        }
        pagamentos.forEach { pagamento ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text(pagamento.alunoNome ?: "", fontWeight = FontWeight.Bold)
                    TextoSecundario(
                        "${pagamento.planoNome ?: ""} • ${formatarValor(pagamento.valor)} • ${formaPagamentoLabel(pagamento.formaPagamento)}"
                    )
                    if (pagamento.status == "confirmado") {
                        TextoSecundario(
                            "Confirmado em ${formatarDataHora(pagamento.dataPagamento)} por ${pagamento.confirmadoPorNome ?: "-"}"
                        )
                    }
                    pagamento.observacao?.takeIf { it.isNotBlank() }?.let { TextoSecundario(it) }
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    EtiquetaStatus(pagamento.status == "confirmado")
                    if (pagamento.status == "pendente") {
                        OutlinedButton(onClick = {
                            observacao = ""
                            confirmando = pagamento
                        }) { Text("Confirmar") }
                    }
                }
            }
            HorizontalDivider()
        }
    }

    confirmando?.let { pagamento ->
        DialogoFormulario(
            titulo = "Confirmar pagamento",
            mensagem = "Confirmar pagamento de ${pagamento.alunoNome} — ${formatarValor(pagamento.valor)} via ${formaPagamentoLabel(pagamento.formaPagamento)}?",
            textoConfirmar = "Confirmar",
            aoConfirmar = { confirmar(pagamento) },
            aoCancelar = { confirmando = null }
        ) {
            CampoTexto(observacao, { observacao = it }, "Observação (opcional)")
        }
    }
}

// ---------- Anamnese ----------

@Composable
fun TelaAnamneseLista(abrirAnamnese: (Aluno) -> Unit) {
    val context = LocalContext.current
    var alunos by remember { mutableStateOf<List<Aluno>>(emptyList()) }
    var filtro by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            alunos = BancoLocal.listarAlunos()
        } catch (e: Exception) {
            mostrarToast(context, mensagemDeErro(e, "Erro ao carregar alunos."))
        }
    }

    val filtrados = filtrarAlunos(alunos, filtro)

    ConteudoTela {
        CampoBusca(filtro, { filtro = it }, "Buscar aluno pelo nome")
        Text("Selecione um aluno para ver ou preencher a ficha de anamnese.")

        when {
            alunos.isEmpty() -> TextoVazio("Nenhum aluno cadastrado ainda.")
            filtrados.isEmpty() -> TextoVazio("Nenhum aluno encontrado com esse nome.")
        }
        filtrados.forEach { aluno ->
            ItemAluno(aluno, aoClicar = { abrirAnamnese(aluno) }, iconeInicio = true)
        }
    }
}

// pergunta, exemplo (opcional) e placeholder de cada campo da ficha
private class PerguntaAnamnese(val pergunta: String, val exemplo: String?, val placeholder: String)

private val PERGUNTAS = listOf(
    PerguntaAnamnese("Pratica alguma atividade desportiva atualmente?", null, "Ex: Não / Sim, musculação"),
    PerguntaAnamnese("Praticou alguma atividade física/desporto anteriormente?", null, "Ex: Não / Sim, futebol até 2020"),
    PerguntaAnamnese("Teve ou tem problemas osteoarticulares?", "(ex.: fraturas, lordoses, hérnias)", "Ex: Não"),
    PerguntaAnamnese("Teve ou tem problemas neuromusculares?", "(ex.: entorses, edemas, ruturas de ligamentos, micro/macro ruturas)", "Ex: Não"),
    PerguntaAnamnese("Sofre de problemas coronários? Risco familiar?", null, "Ex: Não"),
    PerguntaAnamnese("Sofre de problemas vasculares?", null, "Ex: Não"),
    PerguntaAnamnese("Foi hospitalizado nos últimos cinco anos?", null, "Ex: Não"),
    PerguntaAnamnese("Fez alguma cirurgia nos últimos cinco anos?", null, "Ex: Não")
)

@Composable
fun TelaAlunoAnamnese(alunoId: Int, aoSalvar: () -> Unit) {
    val context = LocalContext.current
    val escopo = rememberCoroutineScope()

    // 8 respostas + nome, telefone e parentesco do contato de emergência
    val respostas = remember { mutableStateListOf(*Array(11) { "" }) }
    var carregando by remember { mutableStateOf(true) }
    var salvando by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            BancoLocal.buscarAnamnese(alunoId)?.let { a ->
                listOf(
                    a.praticaAtividadeAtualmente, a.praticouAtividadeAnteriormente, a.problemaOsteoarticular,
                    a.problemaNeuromuscular, a.problemaCoronario, a.problemaVascular, a.hospitalizado5Anos,
                    a.cirurgia5Anos, a.contatoEmergenciaNome, a.contatoEmergenciaTelefone, a.contatoEmergenciaParentesco
                ).forEachIndexed { i, valor -> respostas[i] = valor ?: "" }
            }
        } catch (e: Exception) {
            mostrarToast(context, mensagemDeErro(e, "Erro ao carregar anamnese."))
        }
        carregando = false
    }

    fun salvar() {
        salvando = true
        val r = respostas.map { it.trim() }
        escopo.launch {
            try {
                BancoLocal.salvarAnamnese(
                    alunoId,
                    Anamnese(r[0], r[1], r[2], r[3], r[4], r[5], r[6], r[7], r[8], r[9], r[10])
                )
                mostrarToast(context, "Anamnese salva com sucesso!")
                aoSalvar()
            } catch (e: Exception) {
                mostrarToast(context, mensagemDeErro(e, "Erro ao salvar anamnese."))
            } finally {
                salvando = false
            }
        }
    }

    ConteudoTela {
        if (carregando) {
            Carregando()
            return@ConteudoTela
        }

        PERGUNTAS.forEachIndexed { i, pergunta ->
            Text(pergunta.pergunta, modifier = Modifier.padding(top = Espaco.pequeno))
            pergunta.exemplo?.let { TextoSecundario(it) }
            CampoTexto(respostas[i], { respostas[i] = it }, "Resposta", pergunta.placeholder)
        }

        TituloSecao("Contato de Emergência")
        CampoTexto(respostas[8], { respostas[8] = it }, "Nome", "Ex: Maria Silva")
        CampoTexto(respostas[9], { respostas[9] = it }, "Telefone", "Ex: (51) 99999-9999", KeyboardType.Phone)
        CampoTexto(respostas[10], { respostas[10] = it }, "Grau de parentesco", "Ex: Mãe, Pai, Cônjuge...")

        Button(
            onClick = { salvar() },
            enabled = !salvando && respostas.all { it.isNotBlank() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Espaco.medio)
        ) {
            Text(if (salvando) "Salvando..." else "SALVAR ANAMNESE")
        }
    }
}
