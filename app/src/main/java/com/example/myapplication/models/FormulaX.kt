package com.example.myapplication.models // Certifique-se de que o package está correto

import com.google.gson.annotations.SerializedName

typealias Variables = Map<String, String>

typealias Constants = Map<String, String>

data class FormulaX(
    // Se 'constants' no seu JSON é um objeto com pares chave-valor, ou null
    @SerializedName("constants") val constants: Constants?,

    @SerializedName("description") val description: String,
    @SerializedName("latex") val latex: List<String>,
    @SerializedName("name") val name: String,

    // 'variables' agora será um Map<String, String>?
    @SerializedName("variables") val variables: Variables?,

    // Parâmetro de expandido ou não da fórmula
    @Transient var isExpanded: Boolean = false
)

// Se você tiver uma classe que representa o arquivo JSON inteiro, ela seria algo como:
// data class SubjectContent(
//    @SerializedName("formulas") val formulas: List<FormulaX>,
//    // ... outros campos do seu JSON raiz
// )
