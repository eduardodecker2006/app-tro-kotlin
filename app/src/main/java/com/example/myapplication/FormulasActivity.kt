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

        // 1. Preenche os dados de origem em cada fórmula
        formulas.forEachIndexed { index, formula ->
            formula.disciplinaOrigem = nomeDisciplina
            formula.arquivoJsonOrigem = fileName
            formula.indiceNoArray = index
        }

        // 2. Atualiza o subtítulo
        val numFormulas = formulas.size
        tvSubtituloFormulas.text =
            if (numFormulas == 1) "1 fórmula disponível" else "$numFormulas fórmulas disponíveis"

        // 3. Encontra o índice exato para focar (para expansão E sinalização)
        val indexParaFocar: Int = if (formulaFocoNome != null) {
            if (formulaFocoIndice >= 0) {
                // Busca por NOME e ÍNDICE (lógica correta)
                formulas.indexOfFirst {
                    it.name.equals(formulaFocoNome, ignoreCase = true) &&
                            it.indiceNoArray == formulaFocoIndice
                }
            } else {
                // Fallback: busca apenas pelo nome
                formulas.indexOfFirst { it.name.equals(formulaFocoNome, ignoreCase = true) }
            }
        } else {
            -1 // Nenhum foco solicitado
        }

        Log.d("FormulasActivity",
            "Buscando fórmula: nome='$formulaFocoNome', índice=$formulaFocoIndice, " +
                    "posição encontrada=$indexParaFocar")

        // 4. Pré-expande a fórmula correta, se encontrada
        if (indexParaFocar != -1) {
            // Isso garante que a fórmula correta já comece expandida
            formulas[indexParaFocar].isExpanded = true
        }

        // 5. Configurar o adapter
        // MODIFICAÇÃO: Passamos o 'indexParaFocar' (Int) em vez de 'null' ou um nome.
        // O adapter agora usará este índice para a animação de sinalização.
        formulasAdapter = FormulasAdapter(this, formulas, indexParaFocar) { formula ->
            android.util.Log.d("FormulasActivity", "Fórmula clicada: ${formula.name}")
        }
        recyclerView.adapter = formulasAdapter

        // 6. Executar a rolagem para o item focado
        if (indexParaFocar != -1) {
            recyclerView.post {
                // Rola imediatamente para a posição
                (recyclerView.layoutManager as? LinearLayoutManager)?.scrollToPosition(
                    indexParaFocar
                )

                // Post aninhado para centralizar após a primeira rolagem
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
                "⚠ Fórmula não encontrada: nome='$formulaFocoNome', índice=$formulaFocoIndice")
        }
    }
}