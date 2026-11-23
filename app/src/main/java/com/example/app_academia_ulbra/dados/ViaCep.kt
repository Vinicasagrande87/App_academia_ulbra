package com.example.app_academia_ulbra.dados

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

data class Endereco(
    val cep: String? = null,
    val logradouro: String? = null,
    val bairro: String? = null,
    val localidade: String? = null,
    val uf: String? = null,
    val erro: Boolean? = null
)

interface ViaCepApi {
    @GET("ws/{cep}/json/")
    suspend fun buscar(@Path("cep") cep: String): Endereco
}

object ViaCep {
    val api: ViaCepApi = Retrofit.Builder()
        .baseUrl("https://viacep.com.br/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ViaCepApi::class.java)
}

class CepNaoEncontrado : Exception("CEP não encontrado")

// o ViaCEP responde 200 com {"erro": true} quando o CEP não existe
suspend fun buscarEndereco(cep: String): Endereco {
    val endereco = ViaCep.api.buscar(cep)
    if (endereco.erro == true) throw CepNaoEncontrado()
    return endereco
}
