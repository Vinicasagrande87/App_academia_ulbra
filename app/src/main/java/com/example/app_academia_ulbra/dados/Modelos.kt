package com.example.app_academia_ulbra.dados

// Os campos numéricos que o banco do site guardava como "numeric" (valor, peso,
// altura, carga) ficam como String aqui, igual vinham da API

data class LoginRequest(val email: String, val senha: String)

data class LoginResposta(
    val id: Int,
    val nome: String,
    val tipo: String,
    val token: String
)

data class Aluno(
    val id: Int = 0,
    val nome: String = "",
    val idade: Int? = null,
    val peso: String? = null,
    val altura: String? = null,
    val cpf: String? = null,
    val telefone: String? = null,
    val email: String = "",
    val finalidade: String? = null
)

data class NovoAluno(
    val nome: String,
    val idade: Int?,
    val peso: Double?,
    val altura: Double?,
    val cpf: String,
    val telefone: String,
    val email: String,
    val senha: String,
    val finalidade: String
)

data class PerfilAluno(
    val nome: String,
    val peso: Double?,
    val altura: Double?,
    val cpf: String,
    val telefone: String,
    val email: String,
    val finalidade: String
)

data class Professor(
    val id: Int = 0,
    val nome: String = "",
    val email: String = "",
    val cref: String? = null,
    val especialidade: String? = null
)

data class NovoProfessor(
    val nome: String,
    val email: String,
    val senha: String,
    val cref: String,
    val especialidade: String
)

data class EdicaoProfessor(
    val nome: String,
    val email: String,
    val cref: String,
    val especialidade: String
)

data class Plano(
    val id: Int = 0,
    val nome: String = "",
    val descricao: String? = null,
    val valor: String? = null,
    val duracaoDias: Int? = null
)

data class PlanoRequest(
    val nome: String,
    val descricao: String,
    val valor: Double,
    val duracaoDias: Int
)

data class Pagamento(
    val id: Int,
    val alunoId: Int,
    val alunoNome: String?,
    val planoId: Int,
    val planoNome: String?,
    val valor: String?,
    val formaPagamento: String?,
    val dataPagamento: String?,
    val validoAte: String?,
    val status: String,
    val confirmadoPorNome: String?,
    val observacao: String?
)

// o aluno só manda o plano e a forma de pagamento (o valor vem do cadastro
// do plano); o professor/admin manda também o aluno, o valor e a observação
data class PagamentoRequest(
    val planoId: Int,
    val formaPagamento: String,
    val alunoId: Int? = null,
    val valor: Double? = null,
    val observacao: String? = null
)

data class ConfirmacaoPagamento(
    val status: String,
    val valor: String?,
    val formaPagamento: String?,
    val observacao: String
)

data class Exercicio(
    val id: Int = 0,
    val nome: String = "",
    val grupoMuscular: String = "",
    val videoUrl: String? = null,
    val equipamento: String? = null
)

data class ExercicioRequest(
    val nome: String,
    val grupoMuscular: String,
    val videoUrl: String,
    val equipamento: String
)

data class ItemTreino(
    val exercicioId: Int,
    val exercicioNome: String?,
    val videoUrl: String?,
    val carga: String?,
    val repeticoes: String?,
    val ordem: Int? = null
)

data class Treino(
    val id: Int,
    val diaSemana: String,
    val exercicios: List<ItemTreino> = emptyList()
)

data class ItemTreinoRequest(
    val exercicioId: Int,
    val carga: Double?,
    val repeticoes: String,
    val ordem: Int
)

data class TreinoRequest(
    val alunoId: Int?,
    val diaSemana: String,
    val exercicios: List<ItemTreinoRequest>
)

data class Anamnese(
    val praticaAtividadeAtualmente: String? = null,
    val praticouAtividadeAnteriormente: String? = null,
    val problemaOsteoarticular: String? = null,
    val problemaNeuromuscular: String? = null,
    val problemaCoronario: String? = null,
    val problemaVascular: String? = null,
    val hospitalizado5Anos: String? = null,
    val cirurgia5Anos: String? = null,
    val contatoEmergenciaNome: String? = null,
    val contatoEmergenciaTelefone: String? = null,
    val contatoEmergenciaParentesco: String? = null
)
