package com.example.myapplication.utils

import android.content.Context
import android.content.SharedPreferences

object FavoritesManager {

    private const val PREFS_NAME = "app_favorites_prefs"
    private const val KEY_FAVORITE_FORMULAS = "favorite_formulas"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }


    fun toggleFormulaFavorite(context: Context, formulaName: String) {
        val prefs = getPreferences(context)
        val favorites = getFormulaFavorites(context).toMutableSet()
        if (favorites.contains(formulaName)) {
            favorites.remove(formulaName)
        } else {
            favorites.add(formulaName)
        }
        prefs.edit().putStringSet(KEY_FAVORITE_FORMULAS, favorites).apply()
    }

    fun isFormulaFavorite(context: Context, formulaName: String): Boolean {
        return getFormulaFavorites(context).contains(formulaName)
    }
    fun getFormulaFavorites(context: Context): Set<String> {
        val prefs = getPreferences(context)
        return prefs.getStringSet(KEY_FAVORITE_FORMULAS, emptySet()) ?: emptySet()
    }
}