package com.example.app_academia_ulbra.dados

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.IOException
import java.util.concurrent.TimeUnit

// o mesmo backend que o site usa (Node + Supabase, hospedado na Vercel)
const val URL_SERVIDOR = "https://ulbra-gym.vercel.app/"

// erro com a mensagem que a API devolve no campo "error"
class ErroApi(val mensagem: String, val codigo: String? = null, val status: Int? = null) : Exception(mensagem)

fun lerErro(e: Throwable): ErroApi = e as? ErroApi ?: ErroApi(e.message ?: "Erro inesperado.")

fun mensagemDeErro(e: Throwable, padrao: String): String = (e as? ErroApi)?.mensagem ?: padrao

// "detalhes" vem quando a validação dos campos recusa o envio
private class RespostaErro(val error: String?, val codigo: String?, val detalhes: List<String>?)

interface ServidorApi {
    @POST("login") suspend fun login(@Body dados: LoginRequest): LoginResposta

    @GET("alunos") suspend fun listarAlunos(): List<Aluno>
    @POST("alunos") suspend fun cadastrarAluno(@Body aluno: NovoAluno): Aluno
    @GET("alunos/perfil") suspend fun meuPerfil(): Aluno
    @PUT("alunos") suspend fun atualizarPerfil(@Body perfil: PerfilAluno)
    @GET("alunos/treino") suspend fun meusTreinos(): List<Treino>

    @GET("professores") suspend fun listarProfessores(): List<Professor>
    @POST("professores") suspend fun cadastrarProfessor(@Body professor: NovoProfessor): Professor
    @PUT("professores/{id}") suspend fun editarProfessor(@Path("id") id: Int, @Body dados: EdicaoProfessor)
    @DELETE("professores/{id}") suspend fun excluirProfessor(@Path("id") id: Int)

    @GET("exercicios") suspend fun listarExercicios(): List<Exercicio>
    @POST("exercicios") suspend fun cadastrarExercicio(@Body exercicio: ExercicioRequest): Exercicio
    @PUT("exercicios/{id}") suspend fun editarExercicio(@Path("id") id: Int, @Body exercicio: ExercicioRequest)

    @GET("treinos") suspend fun listarTreinos(@Query("aluno_id") alunoId: Int): List<Treino>
    @POST("treinos") suspend fun criarTreino(@Body treino: TreinoRequest)
    @PUT("treinos/{id}") suspend fun editarTreino(@Path("id") id: Int, @Body treino: TreinoRequest)
    @DELETE("treinos/{id}") suspend fun excluirTreino(@Path("id") id: Int)

    @GET("planos") suspend fun listarPlanos(): List<Plano>
    @POST("planos") suspend fun criarPlano(@Body plano: PlanoRequest)
    @PUT("planos/{id}") suspend fun editarPlano(@Path("id") id: Int, @Body plano: PlanoRequest)
    @DELETE("planos/{id}") suspend fun excluirPlano(@Path("id") id: Int)

    @GET("pagamentos")
    suspend fun listarPagamentos(@Query("aluno_id") alunoId: Int?, @Query("status") status: String?): List<Pagamento>
    @POST("pagamentos") suspend fun registrarPagamento(@Body pagamento: PagamentoRequest)
    @PUT("pagamentos/{id}") suspend fun confirmarPagamento(@Path("id") id: Int, @Body dados: ConfirmacaoPagamento)

    // a API responde "null" quando o aluno ainda não tem anamnese
    @GET("anamnese/{alunoId}") suspend fun buscarAnamnese(@Path("alunoId") alunoId: Int): Response<Anamnese>
    @PUT("anamnese/{alunoId}") suspend fun salvarAnamnese(@Path("alunoId") alunoId: Int, @Body anamnese: Anamnese)
}

object Servidor {

    // a API usa snake_case (aluno_id, dia_semana...) e o app usa camelCase
    private val gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()

    // manda o token do login em toda requisição, igual o site faz
    private val cliente = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val pedido = chain.request().newBuilder()
            Sessao.token?.let { pedido.header("Authorization", "Bearer $it") }
            chain.proceed(pedido.build())
        }
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val api: ServidorApi = Retrofit.Builder()
        .baseUrl(URL_SERVIDOR)
        .client(cliente)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(ServidorApi::class.java)

    // transforma qualquer falha da chamada num ErroApi com mensagem pra tela
    private suspend fun <T> chamar(bloco: suspend (ServidorApi) -> T): T = try {
        bloco(api)
    } catch (e: HttpException) {
        throw erroDe(e.response()!!)
    } catch (e: IOException) {
        throw ErroApi("Sem conexão com a internet.")
    }

    private fun erroDe(resposta: Response<*>): ErroApi {
        val corpo = try {
            gson.fromJson(resposta.errorBody()?.string(), RespostaErro::class.java)
        } catch (e: Exception) {
            null
        }
        val mensagem = when {
            resposta.code() == 401 -> "Sua sessão expirou. Saia e entre de novo."
            !corpo?.detalhes.isNullOrEmpty() -> corpo!!.detalhes!!.joinToString("\n")
            else -> corpo?.error
        }
        return ErroApi(mensagem ?: "Erro no servidor (${resposta.code()}).", corpo?.codigo, resposta.code())
    }

    // a API manda a carga como "30.00" e o gif do WorkoutX só pelo id
    private fun ajustar(treino: Treino) = treino.copy(
        exercicios = treino.exercicios.orEmpty().map {
            it.copy(carga = formatarCarga(it.carga?.toDoubleOrNull()), videoUrl = it.videoUrl?.takeIf(String::isNotBlank) ?: urlGif(it.workoutxId))
        }
    )

    // ---------- login ----------

    suspend fun login(dados: LoginRequest) = chamar { it.login(dados) }

    // ---------- alunos ----------

    suspend fun listarAlunos() = chamar { it.listarAlunos() }

    suspend fun cadastrarAluno(aluno: NovoAluno) = chamar { it.cadastrarAluno(aluno) }

    suspend fun meuPerfil() = chamar { it.meuPerfil() }

    suspend fun atualizarPerfil(perfil: PerfilAluno) = chamar { it.atualizarPerfil(perfil) }

    // a API responde 404 quando o aluno ainda não tem treino: pra tela é só uma lista vazia
    suspend fun meusTreinos(): List<Treino> = try {
        chamar { it.meusTreinos() }.map(::ajustar)
    } catch (e: ErroApi) {
        if (e.status == 404) emptyList() else throw e
    }

    // ---------- professores ----------

    suspend fun listarProfessores() = chamar { it.listarProfessores() }

    suspend fun cadastrarProfessor(professor: NovoProfessor) = chamar { it.cadastrarProfessor(professor) }

    suspend fun editarProfessor(id: Int, dados: EdicaoProfessor) = chamar { it.editarProfessor(id, dados) }

    suspend fun excluirProfessor(id: Int) = chamar { it.excluirProfessor(id) }

    // ---------- exercícios ----------

    suspend fun listarExercicios() = chamar { it.listarExercicios() }

    suspend fun cadastrarExercicio(exercicio: ExercicioRequest) = chamar { it.cadastrarExercicio(exercicio) }

    suspend fun editarExercicio(id: Int, exercicio: ExercicioRequest) = chamar { it.editarExercicio(id, exercicio) }

    // ---------- treinos ----------

    suspend fun treinosDoAluno(alunoId: Int) = chamar { it.listarTreinos(alunoId) }.map(::ajustar)

    suspend fun criarTreino(treino: TreinoRequest) {
        val alunoId = treino.alunoId ?: throw ErroApi("Informe o aluno.")
        // um treino por dia: a tela de edição abre o treino pelo dia da semana
        if (treinosDoAluno(alunoId).any { it.diaSemana == treino.diaSemana }) {
            throw ErroApi("Esse aluno já tem um treino de ${treino.diaSemana}. Use \"Editar Dia\" na ficha para alterar.")
        }
        chamar { it.criarTreino(treino) }
    }

    suspend fun editarTreino(id: Int, treino: TreinoRequest) = chamar { it.editarTreino(id, treino) }

    suspend fun excluirTreino(id: Int) = chamar { it.excluirTreino(id) }

    // ---------- planos ----------

    suspend fun listarPlanos() = chamar { it.listarPlanos() }

    suspend fun criarPlano(plano: PlanoRequest) = chamar { it.criarPlano(plano) }

    suspend fun editarPlano(id: Int, plano: PlanoRequest) = chamar { it.editarPlano(id, plano) }

    suspend fun excluirPlano(id: Int) = chamar { it.excluirPlano(id) }

    // ---------- pagamentos ----------

    // o aluno só recebe os próprios pagamentos (a API filtra pelo token)
    suspend fun listarPagamentos(alunoId: Int? = null, status: String? = null) =
        chamar { it.listarPagamentos(alunoId, status) }

    suspend fun registrarPagamento(pagamento: PagamentoRequest) = chamar { it.registrarPagamento(pagamento) }

    suspend fun confirmarPagamento(id: Int, dados: ConfirmacaoPagamento) = chamar { it.confirmarPagamento(id, dados) }

    // ---------- anamnese ----------

    suspend fun buscarAnamnese(alunoId: Int): Anamnese? {
        val resposta = chamar { it.buscarAnamnese(alunoId) }
        if (!resposta.isSuccessful) throw erroDe(resposta)
        return resposta.body()
    }

    suspend fun salvarAnamnese(alunoId: Int, anamnese: Anamnese) = chamar { it.salvarAnamnese(alunoId, anamnese) }
}

// gif do exercício passa pelo backend, que guarda a chave do WorkoutX
fun urlGif(workoutxId: String?): String? = workoutxId?.takeIf { it.isNotBlank() }?.let { "${URL_SERVIDOR}workoutx/gif/$it" }
