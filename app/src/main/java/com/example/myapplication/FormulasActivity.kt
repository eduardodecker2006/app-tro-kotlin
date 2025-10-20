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
            carregarFormulasDoArquivo(nomeArquivoJson, nomeDisciplina, formulaFocoNome)
        } else {
            Toast.makeText(this, "Erro: Arquivo da disciplina não encontrado.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    /**
     * *** CORREÇÃO PRINCIPAL AQUI ***
     * Agora recebe também o nome da disciplina para preencher o campo disciplinaOrigem
     */
    private fun carregarFormulasDoArquivo(
        fileName: String,
        nomeDisciplina: String,
        formulaFocoNome: String?
    ) {
        val disciplinaJsonReader = DisciplinaJsonReader()
        val disciplina = disciplinaJsonReader.loadDisciplina(this, fileName)
        val formulas = disciplina?.formulas ?: emptyList()

        // *** CRÍTICO: Preencher os campos de cada fórmula ***
        formulas.forEach { formula ->
            formula.disciplinaOrigem = nomeDisciplina
            formula.arquivoJsonOrigem = fileName
        }

        // Atualizar o subtítulo com o número de fórmulas
        val numFormulas = formulas.size
        tvSubtituloFormulas.text =
            if (numFormulas == 1) "1 fórmula disponível" else "$numFormulas fórmulas disponíveis"

        // Configurar o adapter com as fórmulas
        formulasAdapter = FormulasAdapter(this, formulas, formulaFocoNome) { formula ->
            android.util.Log.d("FormulasActivity", "Fórmula clicada: ${formula.name}")
        }
        recyclerView.adapter = formulasAdapter

        // Lógica de rolagem para a fórmula de foco
        if (formulaFocoNome != null) {
            val indexParaRolar =
                formulas.indexOfFirst { it.name.equals(formulaFocoNome, ignoreCase = true) }

            if (indexParaRolar != -1) {
                recyclerView.post {
                    (recyclerView.layoutManager as? LinearLayoutManager)?.scrollToPosition(
                        indexParaRolar
                    )

                    recyclerView.post {
                        val layoutManager =
                            recyclerView.layoutManager as? LinearLayoutManager ?: return@post
                        val viewDoItem = layoutManager.findViewByPosition(indexParaRolar)

                        if (viewDoItem != null) {
                            val alturaTela = recyclerView.height
                            val alturaItem = viewDoItem.height
                            val posicaoAtualDoItem = viewDoItem.top
                            val offsetDesejado = (alturaTela / 2) - (alturaItem / 2)
                            val distanciaParaRolar = posicaoAtualDoItem - offsetDesejado
                            recyclerView.smoothScrollBy(0, distanciaParaRolar, null, 500)
                        }
                    }
                }
            }
        }
    }
}