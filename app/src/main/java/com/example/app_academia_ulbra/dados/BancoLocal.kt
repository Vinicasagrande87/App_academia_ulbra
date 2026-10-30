package com.example.app_academia_ulbra.dados

import android.content.Context
import com.google.gson.Gson
import java.io.File
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import java.time.LocalDate

// Substitui a API do site: os dados ficam salvos num arquivo dentro do celular
// (banco_local.json), então o que for cadastrado continua lá ao reabrir o app.
// Na primeira vez que o app abre, ele começa com os dados de exemplo abaixo.

// login de cada pessoa: e-mail, senha (guardada só como hash), tipo e o id do cadastro dela.
// Fica salvo junto com os outros dados, então quem é cadastrado no app já consegue entrar
private class Acesso(var email: String, val sal: String, val senhaHash: String, val tipo: String, val id: Int)

private fun gerarSal(): String = ByteArray(16).also { SecureRandom().nextBytes(it) }.joinToString("") { "%02x".format(it) }

private fun hashSenha(senha: String, sal: String): String =
    MessageDigest.getInstance("SHA-256").digest((sal + senha).toByteArray()).joinToString("") { "%02x".format(it) }

private fun novoAcesso(email: String, senha: String, tipo: String, id: Int): Acesso {
    val sal = gerarSal()
    return Acesso(email, sal, hashSenha(senha, sal), tipo, id)
}

// logins de quem já vem nos dados de exemplo (todos com a senha 123456)
private fun acessosDeExemplo() = mutableListOf(
    novoAcesso("vinicius@ulbra.br", "123456", "admin", 1),
    novoAcesso("ana@ulbra.br", "123456", "aluno", 1),
    novoAcesso("bruno@email.com", "123456", "aluno", 2),
    novoAcesso("carla@email.com", "123456", "aluno", 3),
    novoAcesso("marcos@ulbra.br", "123456", "professor", 1),
    novoAcesso("juliana@ulbra.br", "123456", "professor", 2)
)

// erro com a mesma mensagem que a API devolvia no campo "error"
class ErroApi(val mensagem: String, val codigo: String? = null) : Exception(mensagem)

fun lerErro(e: Throwable): ErroApi = e as? ErroApi ?: ErroApi(e.message ?: "Erro inesperado.")

fun mensagemDeErro(e: Throwable, padrao: String): String = (e as? ErroApi)?.mensagem ?: padrao

object BancoLocal {

    // treinos guardados com o id do aluno junto
    private class TreinoSalvo(val id: Int, val alunoId: Int, var diaSemana: String, var itens: List<ItemTreinoRequest>)

    // tudo que o app guarda; é esse objeto que vira o arquivo JSON
    private class Dados(
        val alunos: MutableList<Aluno>,
        val professores: MutableList<Professor>,
        val planos: MutableList<Plano>,
        val exercicios: MutableList<Exercicio>,
        val treinos: MutableList<TreinoSalvo>,
        val pagamentos: MutableList<Pagamento>,
        val anamneses: MutableMap<Int, Anamnese>,
        // pode vir null de um arquivo salvo antes de existir o login no banco
        var acessos: MutableList<Acesso>?
    )

    private val gson = Gson()
    private var arquivo: File? = null

    private fun dadosDeExemplo(): Dados {
        val hoje = LocalDate.now()
        return Dados(
            alunos = mutableListOf(
                Aluno(1, "Ana Souza", 24, "62.5", "1.65", "123.456.789-00", "(51) 99999-1111", "ana@ulbra.br", "Hipertrofia"),
                Aluno(2, "Bruno Lima", 31, "84.0", "1.80", "987.654.321-00", "(51) 99999-2222", "bruno@email.com", "Emagrecimento"),
                Aluno(3, "Carla Mendes", 19, "55.0", "1.60", "111.222.333-44", "(51) 99999-3333", "carla@email.com", "Condicionamento")
            ),
            professores = mutableListOf(
                Professor(1, "Marcos Pereira", "marcos@ulbra.br", "123456-G/RS", "Musculação"),
                Professor(2, "Juliana Costa", "juliana@ulbra.br", "654321-G/RS", "Funcional")
            ),
            planos = mutableListOf(
                Plano(1, "Mensal", "Acesso livre à musculação", "99.90", 30),
                Plano(2, "Trimestral", "3 meses com desconto", "269.90", 90),
                Plano(3, "Anual", "12 meses pelo melhor preço", "899.90", 365)
            ),
            exercicios = mutableListOf(
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
            ),
            treinos = mutableListOf(
                TreinoSalvo(
                    1, 1, "Segunda-feira", listOf(
                        ItemTreinoRequest(1, 30.0, "3x12", 1),
                        ItemTreinoRequest(2, 10.0, "3x12", 2),
                        ItemTreinoRequest(8, 15.0, "3x15", 3)
                    )
                ),
                TreinoSalvo(
                    2, 1, "Quarta-feira", listOf(
                        ItemTreinoRequest(12, 40.0, "4x10", 1),
                        ItemTreinoRequest(13, 120.0, "4x12", 2),
                        ItemTreinoRequest(11, 50.0, "3x12", 3)
                    )
                )
            ),
            pagamentos = mutableListOf(
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
            ),
            anamneses = mutableMapOf(),
            acessos = acessosDeExemplo()
        )
    }

    private var dados = dadosDeExemplo()

    private val alunos get() = dados.alunos
    private val professores get() = dados.professores
    private val planos get() = dados.planos
    private val exercicios get() = dados.exercicios
    private val treinos get() = dados.treinos
    private val pagamentos get() = dados.pagamentos
    private val anamneses get() = dados.anamneses
    private val acessos: MutableList<Acesso>
        get() = dados.acessos ?: acessosDeExemplo().also { dados.acessos = it }

    // chamado uma vez quando o app abre (MainActivity)
    fun iniciar(context: Context) {
        val arq = File(context.filesDir, "banco_local.json")
        arquivo = arq
        if (arq.exists()) {
            try {
                dados = gson.fromJson(arq.readText(), Dados::class.java)
                // arquivo antigo, de antes do login ficar salvo: cria os logins de exemplo
                if (dados.acessos == null) {
                    dados.acessos = acessosDeExemplo()
                    salvar()
                }
            } catch (e: Exception) {
                // arquivo estragado: começa de novo com os dados de exemplo
                dados = dadosDeExemplo()
                salvar()
            }
        } else {
            salvar()
        }
    }

    // grava tudo no arquivo; chamado depois de cada cadastro, edição ou exclusão
    private fun salvar() {
        arquivo?.writeText(gson.toJson(dados))
    }

    // gera o próximo id de uma lista, como o "increments" do banco
    private fun <T> proximoId(lista: List<T>, id: (T) -> Int) = (lista.maxOfOrNull(id) ?: 0) + 1

    private fun temPlanoAtivo(alunoId: Int) = pagamentos.any {
        it.alunoId == alunoId && it.status == "confirmado" && !(lerData(it.validoAte)?.isBefore(LocalDate.now()) ?: true)
    }

    // e-mail já usado por outro login (de qualquer tipo); "dono" é o próprio cadastro que está sendo editado
    private fun emailEmUso(email: String, dono: Acesso? = null) =
        acessos.any { it !== dono && it.email.equals(email.trim(), ignoreCase = true) }

    private fun acessoDe(tipo: String, id: Int) = acessos.firstOrNull { it.tipo == tipo && it.id == id }

    private fun validade(duracaoDias: Int?): String? = duracaoDias?.let { LocalDate.now().plusDays(it.toLong()).toString() }

    private fun montarTreino(t: TreinoSalvo) = Treino(
        t.id,
        t.diaSemana,
        t.itens.sortedBy { it.ordem }.map { item ->
            val ex = exercicios.firstOrNull { it.id == item.exercicioId }
            ItemTreino(item.exercicioId, ex?.nome, ex?.videoUrl, formatarCarga(item.carga), item.repeticoes, item.ordem)
        }
    )

    // ---------- login ----------

    suspend fun login(dados: LoginRequest): LoginResposta {
        val acesso = acessos.firstOrNull {
            it.email.equals(dados.email.trim(), ignoreCase = true) && it.senhaHash == hashSenha(dados.senha, it.sal)
        } ?: throw ErroApi("Login não cadastrado.")

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
        if (alunos.any { it.email.equals(aluno.email, ignoreCase = true) } || emailEmUso(aluno.email)) {
            throw ErroApi("Este e-mail já está cadastrado.")
        }
        if (alunos.any { it.cpf == aluno.cpf }) throw ErroApi("Este CPF já está cadastrado.")
        val novo = Aluno(
            proximoId(alunos) { it.id }, aluno.nome, aluno.idade, aluno.peso?.toString(), aluno.altura?.toString(),
            aluno.cpf, aluno.telefone, aluno.email, aluno.finalidade
        )
        alunos.add(novo)
        acessos.add(novoAcesso(novo.email, aluno.senha, "aluno", novo.id))
        salvar()
        return novo
    }

    suspend fun meuPerfil(): Aluno =
        alunos.firstOrNull { it.id == Sessao.id } ?: throw ErroApi("Aluno não encontrado.")

    suspend fun atualizarPerfil(perfil: PerfilAluno) {
        val i = alunos.indexOfFirst { it.id == Sessao.id }
        if (i < 0) throw ErroApi("Aluno não encontrado.")
        val acesso = acessoDe("aluno", Sessao.id)
        if (emailEmUso(perfil.email, acesso)) throw ErroApi("Este e-mail já está cadastrado.")
        acesso?.email = perfil.email
        alunos[i] = alunos[i].copy(
            nome = perfil.nome, peso = perfil.peso?.toString(), altura = perfil.altura?.toString(),
            cpf = perfil.cpf, telefone = perfil.telefone, email = perfil.email, finalidade = perfil.finalidade
        )
        salvar()
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
        if (emailEmUso(professor.email)) throw ErroApi("Este e-mail já está cadastrado.")
        val novo = Professor(proximoId(professores) { it.id }, professor.nome, professor.email, professor.cref, professor.especialidade)
        professores.add(novo)
        acessos.add(novoAcesso(novo.email, professor.senha, "professor", novo.id))
        salvar()
        return novo
    }

    suspend fun editarProfessor(id: Int, dados: EdicaoProfessor) {
        val i = professores.indexOfFirst { it.id == id }
        if (i < 0) throw ErroApi("Professor não encontrado.")
        val acesso = acessoDe("professor", id)
        if (emailEmUso(dados.email, acesso)) throw ErroApi("Este e-mail já está cadastrado.")
        acesso?.email = dados.email
        professores[i] = Professor(id, dados.nome, dados.email, dados.cref, dados.especialidade)
        salvar()
    }

    suspend fun excluirProfessor(id: Int) {
        professores.removeAll { it.id == id }
        acessos.removeAll { it.tipo == "professor" && it.id == id }
        salvar()
    }

    // ---------- exercícios ----------

    suspend fun listarExercicios(): List<Exercicio> = exercicios.sortedBy { it.nome }

    suspend fun cadastrarExercicio(exercicio: ExercicioRequest): Exercicio {
        val novo = Exercicio(
            proximoId(exercicios) { it.id }, exercicio.nome, exercicio.grupoMuscular,
            exercicio.videoUrl.ifBlank { null }, exercicio.equipamento.ifBlank { null }
        )
        exercicios.add(novo)
        salvar()
        return novo
    }

    suspend fun editarExercicio(id: Int, exercicio: ExercicioRequest) {
        val i = exercicios.indexOfFirst { it.id == id }
        if (i < 0) throw ErroApi("Exercício não encontrado.")
        exercicios[i] = Exercicio(
            id, exercicio.nome, exercicio.grupoMuscular,
            exercicio.videoUrl.ifBlank { null }, exercicio.equipamento.ifBlank { null }
        )
        salvar()
    }

    // ---------- treinos ----------

    suspend fun treinosDoAluno(alunoId: Int): List<Treino> =
        treinos.filter { it.alunoId == alunoId }.map(::montarTreino)

    suspend fun criarTreino(treino: TreinoRequest) {
        val alunoId = treino.alunoId ?: throw ErroApi("Informe o aluno.")
        if (!temPlanoAtivo(alunoId)) {
            throw ErroApi("Este aluno não tem um plano ativo. Confirme o pagamento na tela financeira antes de montar o treino.")
        }
        // um treino por dia: a tela de edição abre o treino pelo dia da semana
        if (treinos.any { it.alunoId == alunoId && it.diaSemana == treino.diaSemana }) {
            throw ErroApi("Esse aluno já tem um treino de ${treino.diaSemana}. Use \"Editar Dia\" na ficha para alterar.")
        }
        treinos.add(TreinoSalvo(proximoId(treinos) { it.id }, alunoId, treino.diaSemana, treino.exercicios))
        salvar()
    }

    suspend fun editarTreino(id: Int, treino: TreinoRequest) {
        val salvo = treinos.firstOrNull { it.id == id } ?: throw ErroApi("Treino não encontrado.")
        salvo.diaSemana = treino.diaSemana
        salvo.itens = treino.exercicios
        salvar()
    }

    suspend fun excluirTreino(id: Int) {
        treinos.removeAll { it.id == id }
        salvar()
    }

    // ---------- planos ----------

    suspend fun listarPlanos(): List<Plano> = planos.toList()

    suspend fun criarPlano(plano: PlanoRequest) {
        planos.add(Plano(proximoId(planos) { it.id }, plano.nome, plano.descricao, plano.valor.toString(), plano.duracaoDias))
        salvar()
    }

    suspend fun editarPlano(id: Int, plano: PlanoRequest) {
        val i = planos.indexOfFirst { it.id == id }
        if (i < 0) throw ErroApi("Plano não encontrado.")
        planos[i] = Plano(id, plano.nome, plano.descricao, plano.valor.toString(), plano.duracaoDias)
        salvar()
    }

    suspend fun excluirPlano(id: Int) {
        // igual ao banco do site: não deixa apagar plano que já tem pagamento
        if (pagamentos.any { it.planoId == id }) {
            throw ErroApi("Erro ao excluir plano (provavelmente já tem pagamentos vinculados).")
        }
        planos.removeAll { it.id == id }
        salvar()
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
        salvar()
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
        salvar()
    }

    // ---------- anamnese ----------

    suspend fun buscarAnamnese(alunoId: Int): Anamnese? = anamneses[alunoId]

    suspend fun salvarAnamnese(alunoId: Int, anamnese: Anamnese) {
        anamneses[alunoId] = anamnese
        salvar()
    }
}
