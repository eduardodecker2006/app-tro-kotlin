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

        // --- LÓGICA DE RECEBIMENTO DE DADOS ATUALIZADA ---
        val nomeDisciplina = intent.getStringExtra("disciplina_nome") ?: "Fórmulas"
        val nomeArquivoJson = intent.getStringExtra("disciplina_arquivo_json")
        // --- NOVO: Recebendo o nome da fórmula específica que deve receber foco ---
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
            // --- ALTERADO: Passa o nome da fórmula de foco para a função de carregamento ---
            carregarFormulasDoArquivo(nomeArquivoJson, formulaFocoNome)
        } else {
            // Se, por algum motivo, o nome do arquivo não foi passado, mostra um erro e fecha a tela.
            Toast.makeText(this, "Erro: Arquivo da disciplina não encontrado.", Toast.LENGTH_LONG)
                .show()
            finish()
        }
    }

    /**
     * Carrega os dados de uma disciplina a partir de um nome de arquivo JSON específico
     * e configura o RecyclerView para exibi-los.
     * --- ALTERADO: A assinatura da função agora aceita um segundo parâmetro opcional ---
     */
    private fun carregarFormulasDoArquivo(fileName: String, formulaFocoNome: String?) {
        val disciplinaJsonReader = DisciplinaJsonReader()

        // Usa o reader para carregar o objeto completo da disciplina a partir do nome do arquivo
        val disciplina = disciplinaJsonReader.loadDisciplina(this, fileName)

        // Pega a lista de fórmulas da disciplina. Se for nula, usa uma lista vazia para evitar crashes.
        val formulas = disciplina?.formulas ?: emptyList()

        // Atualizar o subtítulo com o número de fórmulas
        val numFormulas = formulas.size
        tvSubtituloFormulas.text =
            if (numFormulas == 1) "1 fórmula disponível" else "$numFormulas fórmulas disponíveis"

        // Configurar o adapter com as fórmulas
        formulasAdapter = FormulasAdapter(this, formulas, formulaFocoNome) { formula ->
            // TODO: Implementar o que acontece quando uma fórmula é clicada
            android.util.Log.d("FormulasActivity", "Fórmula clicada: ${formula.name}")
        }
        recyclerView.adapter = formulasAdapter

        // --- NOVO: Lógica para encontrar e rolar até a fórmula de foco ---
        // Verifica se recebemos um nome de fórmula para focar
        // Em FormulasActivity.kt, dentro de carregarFormulasDoArquivo()

        // --- LÓGICA DE ROLAGEM CORRIGIDA PARA CENTRALIZAÇÃO ---
        if (formulaFocoNome != null) {
            val indexParaRolar =
                formulas.indexOfFirst { it.name.equals(formulaFocoNome, ignoreCase = true) }

            if (indexParaRolar != -1) {
                recyclerView.post {
                    // PASSO 1: Rolar imediatamente para a posição.
                    // Isso garante que a RecyclerView crie e posicione a view do item na tela.
                    // O item aparecerá desalinhado (geralmente na parte inferior ou superior).
                    (recyclerView.layoutManager as? LinearLayoutManager)?.scrollToPosition(
                        indexParaRolar
                    )

                    // PASSO 2: Postar uma segunda ação para o ajuste fino.
                    // Este segundo 'post' garante que a ação do Passo 1 foi concluída e a view existe.
                    recyclerView.post {
                        val layoutManager =
                            recyclerView.layoutManager as? LinearLayoutManager ?: return@post
                        // Agora, com a view garantidamente na tela, podemos encontrá-la.
                        val viewDoItem = layoutManager.findViewByPosition(indexParaRolar)

                        if (viewDoItem != null) {
                            val alturaTela = recyclerView.height
                            val alturaItem = viewDoItem.height

                            // A posição 'y' do topo do item na tela.
                            val posicaoAtualDoItem = viewDoItem.top

                            // O offset que queremos para centralizar o item.
                            val offsetDesejado = (alturaTela / 2) - (alturaItem / 2)

                            // A distância que precisamos rolar para fazer o ajuste.
                            val distanciaParaRolar = posicaoAtualDoItem - offsetDesejado

                            // Executa a rolagem suave para o ajuste final.
                            recyclerView.smoothScrollBy(0, distanciaParaRolar, null, 500)
                        }
                    }
                }
            }
        }
    }
}
