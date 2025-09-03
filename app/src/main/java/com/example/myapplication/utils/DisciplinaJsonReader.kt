package com.example.myapplication.utils

import android.content.Context
import com.example.myapplication.models.FormulaX
import com.google.gson.Gson
import com.example.myapplication.models.Subjects
import java.io.IOException

class DisciplinaJsonReader {

    private val gson = Gson()

    fun readDisciplinaFromAssets(context: Context, fileName: String): Subjects? {
        return try {
            val jsonString = context.assets.open(fileName).bufferedReader().use {
                it.readText()
            }
            gson.fromJson(jsonString, Subjects::class.java)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun loadAllDisciplinas(context: Context, fileNames: List<String>): List<Subjects> {
        val disciplinas = mutableListOf<Subjects>()

        fileNames.forEach { fileName ->
            readDisciplinaFromAssets(context, fileName)?.let { disciplina ->
                disciplinas.add(disciplina)
            }
        }

        return disciplinas
    }

    fun getFormulas(context: Context, disciplinaSlug: String): List<FormulaX> {
        // Constrói o nome do arquivo baseado no slug
        val fileName = "$disciplinaSlug.json"
        
        // Lê a disciplina do arquivo
        val disciplina = readDisciplinaFromAssets(context, fileName)
        
        // Retorna as fórmulas da disciplina ou uma lista vazia se não encontrar
        return disciplina?.formulas ?: emptyList()
    }
}