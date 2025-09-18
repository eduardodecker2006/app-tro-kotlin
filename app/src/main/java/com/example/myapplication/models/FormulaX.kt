package com.example.myapplication.models // Certifique-se de que o package está correto

import com.google.gson.annotations.SerializedName

// Define Variables como um apelido para um Mapa.
// A Chave será o símbolo da grandeza (ex: "F"), o Valor será a descrição.
typealias Variables = Map<String, String>

// Faça o mesmo para Constants se a estrutura no JSON for similar (objeto chave-valor)
// Se 'constants' for sempre null ou um tipo diferente, ajuste conforme necessário.
// Se pode ser um objeto como 'variables' ou null:
typealias Constants = Map<String, String>

data class FormulaX(
    // Se 'constants' no seu JSON é um objeto com pares chave-valor, ou null
    @SerializedName("constants") val constants: Constants?,

    @SerializedName("description") val description: String,
    @SerializedName("latex") val latex: List<String>,
    @SerializedName("name") val name: String,

    // 'variables' agora será um Map<String, String>?
    @SerializedName("variables") val variables: Variables?
)

// Se você tiver uma classe que representa o arquivo JSON inteiro, ela seria algo como:
// data class SubjectContent(
//    @SerializedName("formulas") val formulas: List<FormulaX>,
//    // ... outros campos do seu JSON raiz
// )
