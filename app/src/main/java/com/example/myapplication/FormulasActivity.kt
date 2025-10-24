package com.example.myapplication

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adapters.FormulasAdapter
import com.example.myapplication.models.FormulaX
import com.example.myapplication.utils.DisciplinaJsonReader
import android.util.Log
import com.example.myapplication.utils.RecentFormulasManager

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

        val nomeDisciplina = intent.getStringExtra("disciplina_nome") ?: "Fórmulas"
        val nomeArquivoJson = intent.getStringExtra("disciplina_arquivo_json")
        val formulaFocoNome = intent.getStringExtra("formula_nome_foco")
        val formulaFocoIndice = intent.getIntExtra("formula_indice_foco", -1)

        // Define o título da tela
        tvTituloFormulas.text = nomeDisciplina

        // Configurar o clique no botão de voltar
        btnBack.setOnClickListener {
            onBackPressed()
        }

        // Configurar RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Carrega as fórmulas se o nome do arquivo foi recebido com sucesso
        if (nomeArquivoJson != null) {
            carregarFormulasDoArquivo(nomeArquivoJson, nomeDisciplina, formulaFocoNome, formulaFocoIndice)
        } else {
            Toast.makeText(this, "Erro: Arquivo da disciplina não encontrado.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun carregarFormulasDoArquivo(
        fileName: String,
        nomeDisciplina: String,
        formulaFocoNome: String?,
        formulaFocoIndice: Int
    ) {
        val disciplinaJsonReader = DisciplinaJsonReader()
        val disciplina = disciplinaJsonReader.loadDisciplina(this, fileName)
        val formulas = disciplina?.formulas ?: emptyList()

        formulas.forEachIndexed { index, formula ->
            formula.disciplinaOrigem = nomeDisciplina
            formula.arquivoJsonOrigem = fileName
            formula.indiceNoArray = index
        }

        val numFormulas = formulas.size
        tvSubtituloFormulas.text =
            if (numFormulas == 1) "1 fórmula disponível" else "$numFormulas fórmulas disponíveis"

        val indexParaFocar: Int = if (formulaFocoNome != null) {
            formulas.indexOfFirst { it.name.equals(formulaFocoNome, ignoreCase = true) }
        } else {
            -1 // Nenhum foco solicitado
        }

        Log.d("FormulasActivity",
            "Buscando fórmula: nome='$formulaFocoNome', " +
                    "posição encontrada=$indexParaFocar")

        if (indexParaFocar != -1) {
            val focusedFormula = formulas[indexParaFocar]
            focusedFormula.isExpanded = true


            // Registra o acesso inicial à fórmula que veio da HomeTab.
            registerRecentFormula(focusedFormula)
        }

        formulasAdapter = FormulasAdapter(this, formulas, indexParaFocar) { clickedFormula ->
            Log.d("FormulasActivity", "Fórmula clicada: ${clickedFormula.name}")
            registerRecentFormula(clickedFormula)
        }
        recyclerView.adapter = formulasAdapter

        if (indexParaFocar != -1) {
            recyclerView.post {
                (recyclerView.layoutManager as? LinearLayoutManager)?.scrollToPosition(
                    indexParaFocar
                )

                recyclerView.post {
                    val layoutManager =
                        recyclerView.layoutManager as? LinearLayoutManager ?: return@post
                    val viewDoItem = layoutManager.findViewByPosition(indexParaFocar)

                    if (viewDoItem != null) {
                        val alturaTela = recyclerView.height
                        val alturaItem = viewDoItem.height
                        val posicaoAtualDoItem = viewDoItem.top
                        val offsetDesejado = (alturaTela / 2) - (alturaItem / 2)
                        val distanciaParaRolar = posicaoAtualDoItem - offsetDesejado

                        recyclerView.smoothScrollBy(0, distanciaParaRolar, null, 500)

                        Log.d("FormulasActivity", "✓ Rolagem centralizada executada para posição $indexParaFocar")
                    } else {
                        Log.w("FormulasActivity", "View do item não encontrada na posição $indexParaFocar após rolagem inicial")
                    }
                }
            }
        } else if (formulaFocoNome != null) {
            Log.w("FormulasActivity",
                "⚠ Fórmula não encontrada: nome='$formulaFocoNome'")
        }
    }

    /**
     * Registra uma fórmula como acessada recentemente usando o RecentFormulasManager.
     * @param formula
     */
    private fun registerRecentFormula(formula: FormulaX) {
        val formulaId = formula.getUniqueId()
        RecentFormulasManager.addFormula(this, formulaId)
        Log.i("RecentFormulas", "Fórmula '${formula.name}' (ID: $formulaId) registrada como recente.")
    }
}
