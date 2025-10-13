package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class ColaboradoresFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_colaboradores, container, false)

        // Você pode adicionar lógica aqui depois
        val tvTitulo: TextView = view.findViewById(R.id.tv_colaboradores_titulo)
        tvTitulo.text = "Colaboradores"

        return view
    }
}