package com.example.app_academia_ulbra.dados

import java.time.Instant
import java.time.LocalDate

// Substitui a API do site: os dados ficam guardados na memória do app.
// Tudo que for cadastrado volta ao estado inicial quando o app é fechado.

// acessos liberados no app: e-mail, senha, tipo e o id do cadastro da pessoa
private class Acesso(val email: String, val senha: String, val tipo: String, val id: Int)

private val ACESSOS = listOf(
    Acesso("vinicius@ulbra.br", "123456", "admin", 1),
    Acesso("ana@ulbra.br", "123456", "aluno", 1),       // aluna Ana Souza
    Acesso("marcos@ulbra.br", "123456", "professor", 1) // professor Marcos Pereira
)

// erro com a mesma mensagem que a API devolvia no campo "error"
class ErroApi(val mensagem: String, val codigo: String? = null) : Exception(mensagem)

fun lerErro(e: Throwable): ErroApi = e as? ErroApi ?: ErroApi(e.message ?: "Erro inesperado.")

fun mensagemDeErro(e: Throwable, padrao: String): String = (e as? ErroApi)?.mensagem ?: padrao

object BancoLocal {

    private val hoje = LocalDate.now()

    private val alunos = mutableListOf(
        Aluno(1, "Ana Souza", 24, "62.5", "1.65", "123.456.789-00", "(51) 99999-1111", "ana@ulbra.br", "Hipertrofia"),
        Aluno(2, "Bruno Lima", 31, "84.0", "1.80", "987.654.321-00", "(51) 99999-2222", "bruno@email.com", "Emagrecimento"),
        Aluno(3, "Carla Mendes", 19, "55.0", "1.60", "111.222.333-44", "(51) 99999-3333", "carla@email.com", "Condicionamento")
    )

    private val professores = mutableListOf(
        Professor(1, "Marcos Pereira", "marcos@ulbra.br", "123456-G/RS", "Musculação"),
        Professor(2, "Juliana Costa", "juliana@ulbra.br", "654321-G/RS", "Funcional")
    )

    private val planos = mutableListOf(
        Plano(1, "Mensal", "Acesso livre à musculação", "99.90", 30),
        Plano(2, "Trimestral", "3 meses com desconto", "269.90", 90),
        Plano(3, "Anual", "12 meses pelo melhor preço", "899.90", 365)
    )

    private val exercicios = mutableListOf(
        Exercicio(1, "Supino Reto", "Peito", equipamento = "Barra"),
        Exercicio(2, "Crucifixo", "Peito", equipamento = "Halteres"),
        Exercicio(3, "Puxada Frontal", "Costas", equipamento = "Máquina"),
        Exercicio(4, "Remada Curvada", "Costas", equipamento = "Barra"),
        Exercicio(5, "Desenvolvimento", "Ombro", equipamento = "Halteres"),
        Exercicio(6, "Elevação Lateral", "Ombro", equipamento = "Halteres"),
        Exercicio(7, "Rosca Direta", "Braço", equipamento = "Barra"),
        Exercicio(8, "Tríceps Corda", "Braço", equipamento = "Polia"),
        Exercicio(9, "Abdominal Supra", "Abdômen"),
        Exercicio(10, "Prancha", "Abdômen"),
        Exercicio(11, "Elevação Pélvica", "Glúteo", equipamento = "Barra"),
        Exercicio(12, "Agachamento Livre", "Pernas", equipamento = "Barra"),
        Exercicio(13, "Leg Press", "Pernas", equipamento = "Máquina")
    )

    // treinos guardados com o id do aluno junto
    private class TreinoSalvo(val id: Int, val alunoId: Int, var diaSemana: String, var itens: List<ItemTreinoRequest>)

    private val treinos = mutableListOf(
        TreinoSalvo(
            1, 1, "Segunda-feira", listOf(
                ItemTreinoRequest(1, 30, "3x12", 1),
                ItemTreinoRequest(2, 10, "3x12", 2),
                ItemTreinoRequest(8, 15, "3x15", 3)
            )
        ),
        TreinoSalvo(
            2, 1, "Quarta-feira", listOf(
                ItemTreinoRequest(12, 40, "4x10", 1),
                ItemTreinoRequest(13, 120, "4x12", 2),
                ItemTreinoRequest(11, 50, "3x12", 3)
            )
        )
    )

    private val pagamentos = mutableListOf(
        Pagamento(
            1, 1, null, 1, null, "99.90", "pix",
            Instant.now().minusSeconds(10L * 24 * 3600).toString(),
            hoje.plusDays(20).toString(), "confirmado", "Vinicius", null
        ),
        Pagamento(2, 2, null, 2, null, "269.90", "cartao_credito", null, null, "pendente", null, null),
        Pagamento(
            3, 3, null, 1, null, "99.90", "dinheiro",
            Instant.now().minusSeconds(40L * 24 * 3600).toString(),
            hoje.minusDays(10).toString(), "confirmado", "Vinicius", null
        )
    )

    private val anamneses = mutableMapOf<Int, Anamnese>()

    // gera o próximo id de uma lista, como o "increments" do banco
    private fun <T> proximoId(lista: List<T>, id: (T) -> Int) = (lista.maxOfOrNull(id) ?: 0) + 1

    private fun temPlanoAtivo(alunoId: Int) = pagamentos.any {
        it.alunoId == alunoId && it.status == "confirmado" && !(lerData(it.validoAte)?.isBefore(LocalDate.now()) ?: true)
    }

    private fun validade(duracaoDias: Int?): String? = duracaoDias?.let { LocalDate.now().plusDays(it.toLong()).toString() }

    private fun montarTreino(t: TreinoSalvo) = Treino(
        t.id,
        t.diaSemana,
        t.itens.sortedBy { it.ordem }.map { item ->
            val ex = exercicios.firstOrNull { it.id == item.exercicioId }
            ItemTreino(item.exercicioId, ex?.nome, ex?.videoUrl, item.carga?.toString(), item.repeticoes, item.ordem)
        }
    )

    // ---------- login ----------

    suspend fun login(dados: LoginRequest): LoginResposta {
        val acesso = ACESSOS.firstOrNull { it.email.equals(dados.email, ignoreCase = true) && it.senha == dados.senha }
            ?: throw ErroApi("Login não cadastrado.")

        // o nome vem do cadastro, igual a API fazia
        val nome = when (acesso.tipo) {
            "aluno" -> alunos.firstOrNull { it.id == acesso.id }?.nome
            "professor" -> professores.firstOrNull { it.id == acesso.id }?.nome
            else -> "Vinicius"
        } ?: ""
        return LoginResposta(acesso.id, nome, acesso.tipo, "acesso-local")
    }

    // ---------- alunos ----------

    suspend fun listarAlunos(): List<Aluno> = alunos.sortedBy { it.nome }

    suspend fun cadastrarAluno(aluno: NovoAluno): Aluno {
        if (alunos.any { it.email.equals(aluno.email, ignoreCase = true) }) throw ErroApi("Este e-mail já está cadastrado.")
        if (alunos.any { it.cpf == aluno.cpf }) throw ErroApi("Este CPF já está cadastrado.")
        val novo = Aluno(
            proximoId(alunos) { it.id }, aluno.nome, aluno.idade, aluno.peso?.toString(), aluno.altura?.toString(),
            aluno.cpf, aluno.telefone, aluno.email, aluno.finalidade
        )
        alunos.add(novo)
        return novo
    }

    suspend fun meuPerfil(): Aluno =
        alunos.firstOrNull { it.id == Sessao.id } ?: throw ErroApi("Aluno não encontrado.")

    suspend fun atualizarPerfil(perfil: PerfilAluno) {
        val i = alunos.indexOfFirst { it.id == Sessao.id }
        if (i < 0) throw ErroApi("Aluno não encontrado.")
        alunos[i] = alunos[i].copy(
            nome = perfil.nome, peso = perfil.peso?.toString(), altura = perfil.altura?.toString(),
            cpf = perfil.cpf, telefone = perfil.telefone, email = perfil.email, finalidade = perfil.finalidade
        )
    }

    suspend fun meusTreinos(): List<Treino> {
        if (!temPlanoAtivo(Sessao.id)) {
            throw ErroApi(
                "Seu plano venceu ou ainda não foi confirmado. Procure a recepção para renovar e voltar a acessar seus treinos.",
                "PLANO_INATIVO"
            )
        }
        return treinos.filter { it.alunoId == Sessao.id }.map(::montarTreino).filter { it.exercicios.isNotEmpty() }
    }

    // ---------- professores ----------

    suspend fun listarProfessores(): List<Professor> = professores.sortedBy { it.nome }

    suspend fun cadastrarProfessor(professor: NovoProfessor): Professor {
        val novo = Professor(proximoId(professores) { it.id }, professor.nome, professor.email, professor.cref, professor.especialidade)
        professores.add(novo)
        return novo
    }

    suspend fun editarProfessor(id: Int, dados: EdicaoProfessor) {
        val i = professores.indexOfFirst { it.id == id }
        if (i < 0) throw ErroApi("Professor não encontrado.")
        professores[i] = Professor(id, dados.nome, dados.email, dados.cref, dados.especialidade)
    }

    suspend fun excluirProfessor(id: Int) {
        professores.removeAll { it.id == id }
    }

    // ---------- exercícios ----------

    suspend fun listarExercicios(): List<Exercicio> = exercicios.sortedBy { it.nome }

    suspend fun cadastrarExercicio(exercicio: ExercicioRequest): Exercicio {
        val novo = Exercicio(
            proximoId(exercicios) { it.id }, exercicio.nome, exercicio.grupoMuscular,
            exercicio.videoUrl.ifBlank { null }, exercicio.equipamento.ifBlank { null }
        )
        exercicios.add(novo)
        return novo
    }

    suspend fun editarExercicio(id: Int, exercicio: ExercicioRequest) {
        val i = exercicios.indexOfFirst { it.id == id }
        if (i < 0) throw ErroApi("Exercício não encontrado.")
        exercicios[i] = Exercicio(
            id, exercicio.nome, exercicio.grupoMuscular,
            exercicio.videoUrl.ifBlank { null }, exercicio.equipamento.ifBlank { null }
        )
    }

    // ---------- treinos ----------

    suspend fun treinosDoAluno(alunoId: Int): List<Treino> =
        treinos.filter { it.alunoId == alunoId }.map(::montarTreino)

    suspend fun criarTreino(treino: TreinoRequest) {
        val alunoId = treino.alunoId ?: throw ErroApi("Informe o aluno.")
        if (!temPlanoAtivo(alunoId)) {
            throw ErroApi("Este aluno não tem um plano ativo. Confirme o pagamento na tela financeira antes de montar o treino.")
        }
        treinos.add(TreinoSalvo(proximoId(treinos) { it.id }, alunoId, treino.diaSemana, treino.exercicios))
    }

    suspend fun editarTreino(id: Int, treino: TreinoRequest) {
        val salvo = treinos.firstOrNull { it.id == id } ?: throw ErroApi("Treino não encontrado.")
        salvo.diaSemana = treino.diaSemana
        salvo.itens = treino.exercicios
    }

    suspend fun excluirTreino(id: Int) {
        treinos.removeAll { it.id == id }
    }

    // ---------- planos ----------

    suspend fun listarPlanos(): List<Plano> = planos.toList()

    suspend fun criarPlano(plano: PlanoRequest) {
        planos.add(Plano(proximoId(planos) { it.id }, plano.nome, plano.descricao, plano.valor.toString(), plano.duracaoDias))
    }

    suspend fun editarPlano(id: Int, plano: PlanoRequest) {
        val i = planos.indexOfFirst { it.id == id }
        if (i < 0) throw ErroApi("Plano não encontrado.")
        planos[i] = Plano(id, plano.nome, plano.descricao, plano.valor.toString(), plano.duracaoDias)
    }

    suspend fun excluirPlano(id: Int) {
        // igual ao banco do site: não deixa apagar plano que já tem pagamento
        if (pagamentos.any { it.planoId == id }) {
            throw ErroApi("Erro ao excluir plano (provavelmente já tem pagamentos vinculados).")
        }
        planos.removeAll { it.id == id }
    }

    // ---------- pagamentos ----------

    suspend fun listarPagamentos(alunoId: Int? = null, status: String? = null): List<Pagamento> {
        val filtroAluno = if (Sessao.isAluno) Sessao.id else alunoId
        return pagamentos
            .filter { filtroAluno == null || it.alunoId == filtroAluno }
            .filter { status == null || it.status == status }
            .sortedByDescending { it.id }
            .map { p ->
                p.copy(
                    alunoNome = alunos.firstOrNull { it.id == p.alunoId }?.nome,
                    planoNome = planos.firstOrNull { it.id == p.planoId }?.nome
                )
            }
    }

    suspend fun registrarPagamento(pagamento: PagamentoRequest) {
        val plano = planos.firstOrNull { it.id == pagamento.planoId } ?: throw ErroApi("Plano não encontrado.")
        val id = proximoId(pagamentos) { it.id }

        if (Sessao.isAluno) {
            // o aluno só solicita; o valor vem sempre do cadastro do plano
            pagamentos.add(
                Pagamento(id, Sessao.id, null, plano.id, null, plano.valor, pagamento.formaPagamento, null, null, "pendente", null, null)
            )
        } else {
            val alunoId = pagamento.alunoId
            val valor = pagamento.valor
            if (alunoId == null || valor == null || valor <= 0) {
                throw ErroApi("Informe o aluno e um valor válido para o pagamento.")
            }
            // registrado na recepção: já entra confirmado
            pagamentos.add(
                Pagamento(
                    id, alunoId, null, plano.id, null, valor.toString(), pagamento.formaPagamento,
                    Instant.now().toString(), validade(plano.duracaoDias), "confirmado", Sessao.nome, pagamento.observacao
                )
            )
        }
    }

    suspend fun confirmarPagamento(id: Int, dados: ConfirmacaoPagamento) {
        val i = pagamentos.indexOfFirst { it.id == id }
        if (i < 0) throw ErroApi("Pagamento não encontrado.")
        val plano = planos.firstOrNull { it.id == pagamentos[i].planoId }
        pagamentos[i] = pagamentos[i].copy(
            status = dados.status,
            observacao = dados.observacao.ifBlank { null },
            dataPagamento = Instant.now().toString(),
            validoAte = validade(plano?.duracaoDias),
            confirmadoPorNome = Sessao.nome
        )
    }

    // ---------- anamnese ----------

    suspend fun buscarAnamnese(alunoId: Int): Anamnese? = anamneses[alunoId]

    suspend fun salvarAnamnese(alunoId: Int, anamnese: Anamnese) {
        anamneses[alunoId] = anamnese
    }
}
