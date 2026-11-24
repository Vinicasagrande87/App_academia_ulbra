package com.example.app_academia_ulbra

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.example.app_academia_ulbra.dados.BancoLocal
import com.example.app_academia_ulbra.dados.EdicaoProfessor
import com.example.app_academia_ulbra.dados.Exercicio
import com.example.app_academia_ulbra.dados.ExercicioRequest
import com.example.app_academia_ulbra.dados.GRUPOS_MUSCULARES
import com.example.app_academia_ulbra.dados.NovoProfessor
import com.example.app_academia_ulbra.dados.Plano
import com.example.app_academia_ulbra.dados.PlanoRequest
import com.example.app_academia_ulbra.dados.Professor
import com.example.app_academia_ulbra.dados.Sessao
import com.example.app_academia_ulbra.dados.formatarValor
import com.example.app_academia_ulbra.dados.lerNumero
import com.example.app_academia_ulbra.dados.mensagemDeErro
import com.example.app_academia_ulbra.ui.theme.Espaco
import com.example.app_academia_ulbra.ui.theme.VermelhoPerigo
import kotlinx.coroutines.launch

// botões "Editar" e "Excluir" que aparecem nas listas de professores e planos
@Composable
private fun BotoesEditarExcluir(aoEditar: () -> Unit, aoExcluir: () -> Unit) {
    Row {
        OutlinedButton(onClick = aoEditar) { Text("Editar") }
        Spacer(Modifier.width(4.dp))
        OutlinedButton(
            onClick = aoExcluir,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = VermelhoPerigo)
        ) { Text("Excluir") }
    }
}

// ---------- Professores ----------

@Composable
fun TelaProfessorCadastro(aoSalvar: () -> Unit) {
    val context = LocalContext.current
    val escopo = rememberCoroutineScope()

    var nome by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var senha by rememberSaveable { mutableStateOf("") }
    var cref by rememberSaveable { mutableStateOf("") }
    var especialidade by rememberSaveable { mutableStateOf("") }
    var salvando by remember { mutableStateOf(false) }

    val podeSalvar = nome.isNotBlank() && email.isNotBlank() && email.contains("@") && senha.length >= 6

    fun cadastrar() {
        if (!podeSalvar || salvando) return
        salvando = true
        escopo.launch {
            try {
                BancoLocal.cadastrarProfessor(NovoProfessor(nome.trim(), email.trim(), senha, cref.trim(), especialidade.trim()))
                mostrarToast(context, "Professor cadastrado com sucesso!")
                aoSalvar()
            } catch (e: Exception) {
                mostrarToast(context, mensagemDeErro(e, "Erro ao cadastrar professor."))
            } finally {
                salvando = false
            }
        }
    }

    ConteudoTela {
        CampoTexto(nome, { nome = it }, "Nome completo", "Nome completo")
        CampoTexto(email, { email = it }, "E-mail", "professor@academia.com", KeyboardType.Email)
        CampoTexto(
            senha, { senha = it }, "Senha", "Senha inicial", KeyboardType.Password, senha = true,
            erro = if (senha.isNotEmpty() && senha.length < 6) "Mínimo 6 caracteres" else null
        )
        CampoTexto(cref, { cref = it }, "CREF", "Ex: 123456-G/RS")
        CampoTexto(especialidade, { especialidade = it }, "Especialidade", "Ex: Musculação, Funcional")

        Button(
            onClick = { cadastrar() },
            enabled = podeSalvar && !salvando,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Espaco.medio)
        ) {
            Text("SALVAR PROFESSOR")
        }
    }
}

@Composable
fun TelaProfessoresLista(cadastrarNovo: () -> Unit) {
    val context = LocalContext.current
    val escopo = rememberCoroutineScope()

    var professores by remember { mutableStateOf<List<Professor>>(emptyList()) }
    var recarregar by remember { mutableStateOf(0) }
    var editando by remember { mutableStateOf<Professor?>(null) }
    var excluindo by remember { mutableStateOf<Professor?>(null) }

    // campos do diálogo de edição
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var cref by remember { mutableStateOf("") }
    var especialidade by remember { mutableStateOf("") }

    LaunchedEffect(recarregar) {
        try {
            professores = BancoLocal.listarProfessores()
        } catch (e: Exception) {
            mostrarToast(context, mensagemDeErro(e, "Erro ao carregar professores."))
        }
    }

    fun salvarEdicao(id: Int) {
        // mesma regra do cadastro: o diálogo só fecha com nome e e-mail válidos
        if (nome.isBlank() || !email.contains("@")) {
            mostrarToast(context, "Preencha o nome e um e-mail válido.")
            return
        }
        editando = null
        escopo.launch {
            try {
                BancoLocal.editarProfessor(id, EdicaoProfessor(nome.trim(), email.trim(), cref.trim(), especialidade.trim()))
                mostrarToast(context, "Professor atualizado com sucesso!")
                recarregar++
            } catch (e: Exception) {
                mostrarToast(context, mensagemDeErro(e, "Erro ao atualizar professor."))
            }
        }
    }

    fun excluir(professor: Professor) {
        excluindo = null
        escopo.launch {
            try {
                BancoLocal.excluirProfessor(professor.id)
                professores = professores.filter { it.id != professor.id }
                mostrarToast(context, "Professor excluído com sucesso!")
            } catch (e: Exception) {
                mostrarToast(context, mensagemDeErro(e, "Erro ao excluir professor."))
            }
        }
    }

    ConteudoTela {
        Button(onClick = cadastrarNovo, modifier = Modifier.fillMaxWidth()) {
            Text("+ Cadastrar novo professor")
        }

        if (professores.isEmpty()) {
            TextoVazio("Nenhum professor cadastrado ainda.")
        }
        professores.forEach { professor ->
            Column(Modifier.padding(vertical = Espaco.pequeno)) {
                Text(professor.nome, fontWeight = FontWeight.Bold)
                TextoSecundario(professor.email)
                professor.cref?.takeIf { it.isNotBlank() }?.let { TextoSecundario("CREF: $it") }
                professor.especialidade?.takeIf { it.isNotBlank() }?.let { TextoSecundario(it) }
                BotoesEditarExcluir(
                    aoEditar = {
                        nome = professor.nome
                        email = professor.email
                        cref = professor.cref ?: ""
                        especialidade = professor.especialidade ?: ""
                        editando = professor
                    },
                    aoExcluir = { excluindo = professor }
                )
            }
            HorizontalDivider()
        }
    }

    editando?.let { professor ->
        DialogoFormulario(
            titulo = "Editar professor",
            textoConfirmar = "Salvar",
            aoConfirmar = { salvarEdicao(professor.id) },
            aoCancelar = { editando = null }
        ) {
            CampoTexto(nome, { nome = it }, "Nome")
            CampoTexto(email, { email = it }, "E-mail", teclado = KeyboardType.Email)
            CampoTexto(cref, { cref = it }, "CREF")
            CampoTexto(especialidade, { especialidade = it }, "Especialidade")
        }
    }

    excluindo?.let { professor ->
        DialogoConfirmacao(
            titulo = "Excluir professor",
            mensagem = "Tem certeza que deseja excluir ${professor.nome}? Essa ação não pode ser desfeita.",
            textoConfirmar = "Excluir",
            perigo = true,
            aoConfirmar = { excluir(professor) },
            aoCancelar = { excluindo = null }
        )
    }
}

// ---------- Planos ----------

@Composable
fun TelaPlanosGerenciar() {
    val context = LocalContext.current
    val escopo = rememberCoroutineScope()

    var planos by remember { mutableStateOf<List<Plano>>(emptyList()) }
    var recarregar by remember { mutableStateOf(0) }
    // null = diálogo fechado; Plano() sem id = criando um novo
    var editando by remember { mutableStateOf<Plano?>(null) }
    var excluindo by remember { mutableStateOf<Plano?>(null) }

    var nome by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var valor by remember { mutableStateOf("") }
    var duracao by remember { mutableStateOf("") }

    LaunchedEffect(recarregar) {
        try {
            planos = BancoLocal.listarPlanos()
        } catch (e: Exception) {
            mostrarToast(context, mensagemDeErro(e, "Erro ao carregar planos."))
        }
    }

    fun abrirFormulario(plano: Plano) {
        nome = plano.nome
        descricao = plano.descricao ?: ""
        valor = plano.valor ?: ""
        duracao = plano.duracaoDias?.toString() ?: ""
        editando = plano
    }

    fun salvar(plano: Plano) {
        val valorNumero = lerNumero(valor)
        val duracaoNumero = duracao.trim().toIntOrNull()
        if (nome.isBlank() || valorNumero == null || duracaoNumero == null) {
            mostrarToast(context, "Preencha nome, valor e duração do plano.")
            return
        }
        if (valorNumero <= 0 || duracaoNumero <= 0) {
            mostrarToast(context, "O valor e a duração do plano precisam ser maiores que zero.")
            return
        }
        editando = null
        val dados = PlanoRequest(nome.trim(), descricao.trim(), valorNumero, duracaoNumero)
        val criando = plano.id == 0
        escopo.launch {
            try {
                if (criando) {
                    BancoLocal.criarPlano(dados)
                } else {
                    BancoLocal.editarPlano(plano.id, dados)
                }
                mostrarToast(context, if (criando) "Plano criado com sucesso!" else "Plano atualizado com sucesso!")
                recarregar++
            } catch (e: Exception) {
                mostrarToast(context, mensagemDeErro(e, if (criando) "Erro ao criar plano." else "Erro ao atualizar plano."))
            }
        }
    }

    fun excluir(plano: Plano) {
        excluindo = null
        escopo.launch {
            try {
                BancoLocal.excluirPlano(plano.id)
                planos = planos.filter { it.id != plano.id }
                mostrarToast(context, "Plano excluído com sucesso!")
            } catch (e: Exception) {
                mostrarToast(context, mensagemDeErro(e, "Erro ao excluir plano (provavelmente já tem pagamentos vinculados)."))
            }
        }
    }

    ConteudoTela {
        Button(onClick = { abrirFormulario(Plano()) }, modifier = Modifier.fillMaxWidth()) {
            Text("+ Novo plano")
        }

        if (planos.isEmpty()) {
            TextoVazio("Nenhum plano cadastrado ainda.")
        }
        planos.forEach { plano ->
            Column(Modifier.padding(vertical = Espaco.pequeno)) {
                Text(plano.nome, fontWeight = FontWeight.Bold)
                TextoSecundario("${formatarValor(plano.valor)} • ${plano.duracaoDias ?: "-"} dias")
                plano.descricao?.takeIf { it.isNotBlank() }?.let { TextoSecundario(it) }
                BotoesEditarExcluir(aoEditar = { abrirFormulario(plano) }, aoExcluir = { excluindo = plano })
            }
            HorizontalDivider()
        }
    }

    editando?.let { plano ->
        DialogoFormulario(
            titulo = if (plano.id == 0) "Novo plano" else "Editar plano",
            textoConfirmar = if (plano.id == 0) "Criar" else "Salvar",
            aoConfirmar = { salvar(plano) },
            aoCancelar = { editando = null }
        ) {
            CampoTexto(nome, { nome = it }, "Nome do plano")
            CampoTexto(descricao, { descricao = it }, "Descrição")
            CampoTexto(valor, { valor = it }, "Valor", "Ex: 99.90", KeyboardType.Decimal)
            CampoTexto(duracao, { duracao = it.filter(Char::isDigit) }, "Duração em dias", "Ex: 30", KeyboardType.Number)
        }
    }

    excluindo?.let { plano ->
        DialogoConfirmacao(
            titulo = "Excluir plano",
            mensagem = "Tem certeza que deseja excluir o plano \"${plano.nome}\"? Isso pode falhar se já existirem pagamentos vinculados a ele.",
            textoConfirmar = "Excluir",
            perigo = true,
            aoConfirmar = { excluir(plano) },
            aoCancelar = { excluindo = null }
        )
    }
}

// ---------- Exercícios ----------

@Composable
fun TelaExerciciosLista(editar: (Exercicio) -> Unit) {
    val context = LocalContext.current
    var exercicios by remember { mutableStateOf<List<Exercicio>>(emptyList()) }
    var filtro by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            exercicios = BancoLocal.listarExercicios().sortedWith(compareBy({ it.grupoMuscular }, { it.nome }))
        } catch (e: Exception) {
            mostrarToast(context, mensagemDeErro(e, "Erro ao carregar exercícios."))
        }
    }

    val termo = filtro.trim().lowercase()
    val filtrados = if (termo.isEmpty()) exercicios else exercicios.filter {
        it.nome.lowercase().contains(termo) || it.grupoMuscular.lowercase().contains(termo)
    }

    ConteudoTela {
        CampoBusca(filtro, { filtro = it }, "Buscar por nome ou grupo muscular")

        when {
            exercicios.isEmpty() -> TextoVazio("Nenhum exercício cadastrado ainda.")
            filtrados.isEmpty() -> TextoVazio("Nenhum exercício encontrado com esse termo.")
        }
        filtrados.forEach { ex ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Espaco.pequeno)
            ) {
                Column(Modifier.weight(1f)) {
                    Text(ex.nome, fontWeight = FontWeight.Bold)
                    TextoSecundario(ex.grupoMuscular)
                }
                if (Sessao.isAdmin) {
                    OutlinedButton(onClick = { editar(ex) }) { Text("Editar") }
                }
            }
            HorizontalDivider()
        }
    }
}

private const val OPCAO_NOVO = -1

@Composable
fun TelaExercicioCadastro(
    grupoInicial: String?,
    exercicioIdInicial: Int?,
    verTodos: () -> Unit,
    aoSalvar: () -> Unit
) {
    val context = LocalContext.current
    val escopo = rememberCoroutineScope()

    var todosExercicios by remember { mutableStateOf<List<Exercicio>>(emptyList()) }
    var grupoSelecionado by rememberSaveable { mutableStateOf(grupoInicial ?: "") }
    // id do exercício escolhido na lista, ou OPCAO_NOVO pra cadastrar um novo
    var selecionadoNoSeletor by rememberSaveable { mutableStateOf<Int?>(null) }
    var exercicioEditandoId by rememberSaveable { mutableStateOf<Int?>(null) }

    var nome by rememberSaveable { mutableStateOf("") }
    var videoUrl by rememberSaveable { mutableStateOf("") }
    var equipamento by rememberSaveable { mutableStateOf("") }

    val modoNovo = selecionadoNoSeletor == OPCAO_NOVO
    val exerciciosDoGrupo = todosExercicios.filter { it.grupoMuscular == grupoSelecionado }

    fun resetFormulario() {
        exercicioEditandoId = null
        nome = ""
        videoUrl = ""
        equipamento = ""
    }

    fun selecionarExercicio(id: Int?) {
        selecionadoNoSeletor = id
        val ex = todosExercicios.firstOrNull { it.id == id }
        if (id == OPCAO_NOVO || ex == null) {
            resetFormulario()
            return
        }
        exercicioEditandoId = ex.id
        nome = ex.nome
        videoUrl = ex.videoUrl ?: ""
        equipamento = ex.equipamento ?: ""
    }

    LaunchedEffect(Unit) {
        try {
            todosExercicios = BancoLocal.listarExercicios()
            // veio da lista de exercícios pelo botão "Editar"
            if (exercicioIdInicial != null && selecionadoNoSeletor == null) {
                selecionarExercicio(exercicioIdInicial)
            }
        } catch (e: Exception) {
            mostrarToast(context, mensagemDeErro(e, "Erro ao carregar exercícios."))
        }
    }

    val podeSalvar = when {
        grupoSelecionado.isBlank() -> false
        modoNovo -> nome.isNotBlank()
        else -> exercicioEditandoId != null
    }

    fun salvar() {
        if (!podeSalvar) return
        val dados = ExercicioRequest(nome.trim(), grupoSelecionado, videoUrl.trim(), equipamento.trim())
        val id = exercicioEditandoId
        escopo.launch {
            try {
                if (id != null) {
                    BancoLocal.editarExercicio(id, dados)
                } else {
                    BancoLocal.cadastrarExercicio(dados)
                }
                mostrarToast(context, "Exercício salvo com sucesso!")
                aoSalvar()
            } catch (e: Exception) {
                mostrarToast(context, mensagemDeErro(e, "Erro ao salvar exercício."))
            }
        }
    }

    ConteudoTela {
        Seletor(
            rotulo = "Grupo Muscular",
            opcoes = GRUPOS_MUSCULARES.map { it to it },
            selecionado = grupoSelecionado.ifBlank { null },
            aoSelecionar = {
                grupoSelecionado = it
                selecionadoNoSeletor = null
                resetFormulario()
            },
            placeholder = "Selecione o grupo"
        )
        TextButton(onClick = verTodos) { Text("Ver todos os exercícios cadastrados") }

        if (grupoSelecionado.isBlank()) return@ConteudoTela

        if (!modoNovo) {
            Seletor(
                rotulo = "Exercício",
                opcoes = exerciciosDoGrupo.map { it.id to it.nome } + (OPCAO_NOVO to "+ Cadastrar novo exercício"),
                selecionado = selecionadoNoSeletor,
                aoSelecionar = { selecionarExercicio(it) },
                placeholder = "Selecione um exercício"
            )
            if (exerciciosDoGrupo.isEmpty()) {
                TextoSecundario("Nenhum exercício cadastrado ainda para esse grupo.")
            }
        } else {
            CampoTexto(nome, { nome = it }, "Nome do Exercício", "Ex: Supino Inclinado")
            TextButton(onClick = { selecionarExercicio(null) }) { Text("Voltar para a lista de exercícios") }
        }

        if (modoNovo || exercicioEditandoId != null) {
            // sem a API do WorkoutX, a demonstração é um link de vídeo (ex: YouTube)
            CampoTexto(equipamento, { equipamento = it }, "Equipamento", "Ex: Barra, Halteres, Máquina")
            CampoTexto(videoUrl, { videoUrl = it }, "Link do vídeo (opcional)", "https://www.youtube.com/...", KeyboardType.Uri)
            ExercicioMidia(videoUrl)

            Button(
                onClick = { salvar() },
                enabled = podeSalvar,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Espaco.medio)
            ) {
                Text(if (exercicioEditandoId != null) "ATUALIZAR EXERCÍCIO" else "SALVAR EXERCÍCIO")
            }
        }
    }
}
