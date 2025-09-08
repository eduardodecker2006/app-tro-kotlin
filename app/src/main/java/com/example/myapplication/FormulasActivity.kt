package com.example.myapplication

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
// R já está importado implicitamente pelo nome do pacote se for do mesmo módulo.
// Se R não for encontrado, você pode precisar de:
// import com.example.myapplication.R
import com.example.myapplication.adapters.FormulasAdapter
import com.example.myapplication.models.FormulaX
import com.example.myapplication.utils.DisciplinaJsonReader

class FormulasActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var tvTituloFormulas: TextView
    private lateinit var tvSubtituloFormulas: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var formulasAdapter: FormulasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_formulas)

        // Inicializar views
        btnBack = findViewById(R.id.btn_back)
        tvTituloFormulas = findViewById(R.id.tv_titulo_formulas)
        tvSubtituloFormulas = findViewById(R.id.tv_subtitulo_formulas)
        recyclerView = findViewById(R.id.rv_formulas)

        // Configurar título com o nome da disciplina
        val nomeDisciplina = intent.getStringExtra("disciplina_nome") ?: "Fórmulas"
        val disciplinaSlug = intent.getStringExtra("disciplina_slug") ?: ""
        tvTituloFormulas.text = nomeDisciplina

        // Configurar o clique no botão de voltar
        btnBack.setOnClickListener {
            onBackPressed()
        }

        // Configurar RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Carregar e exibir fórmulas
        carregarFormulas(disciplinaSlug)
    }

    private fun carregarFormulas(disciplinaSlug: String) {
        val disciplinaJsonReader = DisciplinaJsonReader()
        val formulas = disciplinaJsonReader.getFormulas(this, disciplinaSlug)

        // Atualizar o subtítulo com o número de fórmulas
        val numFormulas = formulas.size
        tvSubtituloFormulas.text = "$numFormulas fórmulas disponíveis"

        // Configurar o adapter com as fórmulas
        // **** MODIFICAÇÃO APLICADA AQUI ****
        formulasAdapter = FormulasAdapter(this, formulas) { formula ->
            // TODO: Implementar o que acontece quando uma fórmula é clicada
            // Por exemplo, abrir um Dialog ou uma nova Activity com os detalhes da fórmula
            // Exemplo de log para testar o clique:
            // android.util.Log.d("FormulasActivity", "Fórmula clicada: ${formula.name}")
        }
        recyclerView.adapter = formulasAdapter
    }
}
