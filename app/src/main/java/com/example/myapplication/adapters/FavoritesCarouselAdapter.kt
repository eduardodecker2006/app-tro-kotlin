package com.example.myapplication.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.FormulasActivity
import com.example.myapplication.R
import com.example.myapplication.models.FormulaX

/**
 * Adapter projetado especificamente para o carrossel horizontal de favoritos na tela inicial.
 * Ele recebe uma lista de objetos FormulaX e exibe cada um em um card simples.
 */
class FavoritesCarouselAdapter(
    private val context: Context,
    private val favoriteFormulas: List<FormulaX>
) : RecyclerView.Adapter<FavoritesCarouselAdapter.FavoriteViewHolder>() {

    /**
     * ViewHolder que representa um único card no carrossel de favoritos.
     */
    inner class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val nameTextView: TextView = itemView.findViewById(R.id.tv_favorite_formula_name)
        private val subjectNameTextView: TextView = itemView.findViewById(R.id.tv_favorite_subject_name)

        /**
         * Preenche o card com os dados da fórmula e configura a ação de clique.
         */
        fun bind(formula: FormulaX) {

            nameTextView.text = formula.name
            subjectNameTextView.text = formula.disciplinaOrigem


            Log.d(
                "FavCarouselAdapter",
                "Binding: Fórmula='${formula.name}', Disciplina='${formula.disciplinaOrigem}'"
            )

            itemView.setOnClickListener {
                // Validação para garantir que temos os dados necessários para navegar
                if (formula.disciplinaOrigem.isNullOrBlank() || formula.arquivoJsonOrigem.isNullOrBlank()) {
                    Log.e("FavoritesClick", "Não foi possível navegar: dados de origem ausentes na fórmula '${formula.name}'.")
                    Toast.makeText(context, "Erro ao abrir favorito.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                Log.d("FavoritesClick", "Clicou em '${formula.name}'. Navegando para a disciplina '${formula.disciplinaOrigem}' usando o arquivo '${formula.arquivoJsonOrigem}'.")

                // Cria um Intent para abrir a FormulasActivity, passando os dados necessários
                val intent = Intent(context, FormulasActivity::class.java).apply {
                    // Passa o nome do arquivo JSON da disciplina
                    putExtra("disciplina_arquivo_json", formula.arquivoJsonOrigem)
                    // Passa o nome da disciplina para ser exibido na toolbar
                    putExtra("disciplina_nome", formula.disciplinaOrigem)
                    // Passa o nome da fórmula para que a lista possa rolar até ela
                    putExtra("formula_nome_foco", formula.name)
                }
                // Inicia a activity
                context.startActivity(intent)
            }
        }
    }


     //Chamado quando a RecyclerView precisa de um novo ViewHolder.

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite_card, parent, false)
        return FavoriteViewHolder(view)
    }


     // Chamado pela RecyclerView para exibir os dados em uma posição específica.

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(favoriteFormulas[position])
    }


     //Retorna o número total de itens na lista de favoritos.

    override fun getItemCount() = favoriteFormulas.size
}
