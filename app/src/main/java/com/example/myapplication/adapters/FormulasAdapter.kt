package com.example.myapplication.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.models.FormulaX

class FormulasAdapter(
    private val formulas: List<FormulaX>,
    private val onFormulaClick: (FormulaX) -> Unit
) : RecyclerView.Adapter<FormulasAdapter.FormulaViewHolder>() {

    class FormulaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val formulaName: TextView = view.findViewById(R.id.tv_formula_name)
        val formulaDescription: TextView = view.findViewById(R.id.tv_formula_description)
        val container: View = view
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FormulaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_formula, parent, false)
        return FormulaViewHolder(view)
    }

    override fun onBindViewHolder(holder: FormulaViewHolder, position: Int) {
        val formula = formulas[position]
        with(holder) {
            formulaName.text = formula.name
            formulaDescription.text = formula.description
            container.setOnClickListener { onFormulaClick(formula) }
        }
    }

    override fun getItemCount() = formulas.size
}