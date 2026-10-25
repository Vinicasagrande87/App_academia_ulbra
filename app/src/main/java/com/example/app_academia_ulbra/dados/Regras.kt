package com.example.app_academia_ulbra.dados

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

val DIAS_DA_SEMANA = listOf("Segunda-feira", "Terça-feira", "Quarta-feira", "Quinta-feira", "Sexta-feira", "Sábado")

val GRUPOS_MUSCULARES = listOf("Peito", "Costas", "Ombro", "Braço", "Abdômen", "Glúteo", "Pernas")

// valor que vai pra API -> texto que aparece na tela
val FORMAS_PAGAMENTO = listOf(
    "cartao_credito" to "Cartão de Crédito",
    "cartao_debito" to "Cartão de Débito",
    "pix" to "Pix",
    "dinheiro" to "Dinheiro"
)

fun formaPagamentoLabel(valor: String?): String =
    FORMAS_PAGAMENTO.firstOrNull { it.first == valor }?.second ?: (valor ?: "")

fun ordenarPorDia(treinos: List<Treino>): List<Treino> =
    treinos.sortedBy { DIAS_DA_SEMANA.indexOf(it.diaSemana) }

private val FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy")

// "valido_ate" é uma coluna date: só interessa a parte AAAA-MM-DD
fun lerData(texto: String?): LocalDate? = try {
    texto?.take(10)?.let { LocalDate.parse(it) }
} catch (e: Exception) {
    null
}

fun formatarData(texto: String?): String = lerData(texto)?.format(FORMATO_DATA) ?: ""

// "data_pagamento" é um timestamp (em UTC), então converte pro horário do aparelho
fun formatarDataHora(texto: String?): String = try {
    Instant.parse(texto).atZone(ZoneId.systemDefault()).toLocalDate().format(FORMATO_DATA)
} catch (e: Exception) {
    formatarData(texto)
}

fun formatarValor(valor: String?): String {
    val numero = valor?.toDoubleOrNull() ?: return "R$ ${valor ?: "-"}"
    return "R$ " + String.format(java.util.Locale.forLanguageTag("pt-BR"), "%.2f", numero)
}

// aceita "75,5" ou "75.5"
fun lerNumero(texto: String): Double? = texto.trim().replace(',', '.').toDoubleOrNull()

enum class CorAviso { SUCESSO, ATENCAO, PERIGO, NEUTRO }

class SituacaoPlano(val mensagem: String, val cor: CorAviso)

// mesma regra do calcularSituacaoPlano() do site: olha o pagamento confirmado
// com a validade mais distante e diz se o plano está ativo, vencendo ou vencido
fun calcularSituacaoPlano(pagamentos: List<Pagamento>): SituacaoPlano {
    val planoAtual = pagamentos
        .filter { it.status == "confirmado" && lerData(it.validoAte) != null }
        .maxByOrNull { lerData(it.validoAte)!! }

    if (planoAtual == null) {
        return SituacaoPlano(
            "Você ainda não tem um plano ativo. Escolha um plano abaixo pra começar!",
            CorAviso.NEUTRO
        )
    }

    val validoAte = lerData(planoAtual.validoAte)!!
    val diasRestantes = ChronoUnit.DAYS.between(LocalDate.now(), validoAte)
    val dataFormatada = validoAte.format(FORMATO_DATA)

    return when {
        diasRestantes < 0 -> SituacaoPlano(
            "Seu plano venceu em $dataFormatada. Renove abaixo pra continuar treinando!",
            CorAviso.PERIGO
        )
        diasRestantes <= 7 -> SituacaoPlano(
            "Seu plano vence em $diasRestantes dia${if (diasRestantes == 1L) "" else "s"} ($dataFormatada). Que tal já renovar?",
            CorAviso.ATENCAO
        )
        else -> SituacaoPlano("Seu plano está ativo até $dataFormatada.", CorAviso.SUCESSO)
    }
}
