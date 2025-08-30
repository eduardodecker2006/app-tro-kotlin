package com.example.myapplication.models

data class FormulaX(
    val constants: Any,
    val description: String,
    val latex: List<String>,
    val name: String,
    val variables: Variables
)