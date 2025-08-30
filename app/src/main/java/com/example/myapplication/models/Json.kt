package com.example.myapplication.models

data class Json(
    val date: String,
    val email: Any,
    val github: String,
    val name: String,
    val roles: List<String>
)