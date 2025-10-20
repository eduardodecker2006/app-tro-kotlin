package com.example.myapplication.models

import com.google.gson.annotations.SerializedName

typealias Variables = Map<String, String>
typealias Constants = Map<String, String>

data class FormulaX(

    @SerializedName("constants") val constants: Constants?,
    @SerializedName("description") val description: String,
    @SerializedName("latex") val latex: List<String>,
    @SerializedName("name") val name: String,
    @SerializedName("variables") val variables: Variables?,

    // Parâmetro de expandido ou não da fórmula
    @Transient var isExpanded: Boolean = false,

    /** Implementação do favorito. */
    // Indica se a fórmula é um favorito ou não
    @Transient var isFavorite: Boolean = false,
    // Necessário para o clique no carrossel saber qual disciplina abrir.
    @Transient var disciplinaOrigem: String? = null,
    // Necessário para o clique no carrossel saber qual arquivo carregar.
    @Transient var arquivoJsonOrigem: String? = null
) {
    /**
     * Gera um ID único para a fórmula baseado no arquivo JSON de origem e no nome.
     * Formato: "arquivo-json::nome-da-formula"
     *
     * Exemplo: "analise-de-circuitos-i.json::Lei de Ohm"
     *
     * Se arquivoJsonOrigem for nulo (não deveria acontecer em uso normal),
     * retorna apenas o nome da fórmula como fallback.
     */
    fun getUniqueId(): String {
        return if (arquivoJsonOrigem != null) {
            "$arquivoJsonOrigem::$name"
        } else {
            // Fallback caso arquivoJsonOrigem não esteja definido
            name
        }
    }
}