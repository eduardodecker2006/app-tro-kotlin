package com.example.myapplication.models // Certifique-se de que o package está correto

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
)
