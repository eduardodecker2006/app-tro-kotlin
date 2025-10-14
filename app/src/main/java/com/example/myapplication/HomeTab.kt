package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adapters.SearchAdapter
import com.example.myapplication.models.SearchableItem
import com.example.myapplication.models.Subjects
import com.example.myapplication.utils.DisciplinaJsonReader
import java.util.Locale

class HomeTab : Fragment() {

    /**
     * Data class para representar um par de frase + emoji
     */
    data class PhrasePair(val phrase: String, val emoji: String)

    /**
     * Lista de pares de frases de boas-vindas com seus respectivos emojis.
     * Cada frase tem seu emoji fixo, e um par aleatório é escolhido.
     */
    private val phrasePairs = listOf(
        PhrasePair("Pronto para aprender algo novo hoje?", "💡"),
        PhrasePair("Que tal uma dose de eletrônica?", "🔋"),
        PhrasePair("Seu próximo conhecimento te espera aqui.", "🚀"),
        PhrasePair("O mundo da eletrônica te chama!", "🤖"),
        PhrasePair("Vamos desvendar um mistério da eletrônica?", "📡"),
        PhrasePair("Qual conceito vamos dominar hoje?", "🧠"),
        PhrasePair("Mantenha a mente ligada na eletrônica!", "🔌"),
        PhrasePair("Hora de energizar seus conhecimentos!", "⚡"),
        PhrasePair("Conecte-se com o saber da eletrônica.", "📖"),
        PhrasePair("O tempo passa rápido quando a gente se diverte...", "🤣"),
        PhrasePair("Quem tem mais, tem 15!", "🏆"),
        PhrasePair("Preparado para acender ideias hoje?", "✨"),
        PhrasePair("Cada detalhe aprendido é uma nova conquista.", "🎯"),
        PhrasePair("A aventura da eletrônica nunca para!", "💪"),
        PhrasePair("Qual desafio técnico vamos superar agora?", "📝"),
        PhrasePair("Nunca desligue sua curiosidade!", "🕹️"),
        PhrasePair("A teoria conecta com a prática aqui.", "📚"),
        PhrasePair("Aprender também pode ser diversão!", "😄"),
    )

    // A lista principal para a UI, contendo apenas os dados simples e pesquisáveis.
    private val searchableList = mutableListOf<SearchableItem>()
    private lateinit var searchAdapter: SearchAdapter

    // --- NOVO: Instância do leitor de JSON ---
    private val disciplinaReader = DisciplinaJsonReader()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.tab_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- Configuração da Saudação ---
        setupGreetingMessage(view)

        // --- Configuração da Pesquisa e RecyclerView ---
        setupSearch(view)
    }

    /** Configura a mensagem de boas-vindas com nome, emoji e frase aleatória. */
    private fun setupGreetingMessage(view: View) {
        val welcomeTextView = view.findViewById<TextView>(R.id.welcome_home_textview)
        val sharedPreferences =
            requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val userName = sharedPreferences.getString("user_name", "Usuário") ?: "Usuário"

        // Seleciona um par aleatório de frase + emoji
        val randomPair = phrasePairs.random()

        val greetingPart = "Olá, $userName! ${randomPair.emoji}\n"
        val fullText = greetingPart + randomPair.phrase

        // Aplica a formatação para diminuir o tamanho da segunda linha
        val spannableString = SpannableString(fullText)
        val sizeSpan = RelativeSizeSpan(0.8f)
        val startIndex = greetingPart.length
        val endIndex = fullText.length
        spannableString.setSpan(sizeSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        welcomeTextView.text = spannableString
    }

    /** Configura a RecyclerView e a SearchView para a funcionalidade de busca. */
    private fun setupSearch(view: View) {
        // Carrega os dados dos arquivos JSON
        loadAndTransformContentFromAssets()

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_results)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        searchAdapter = SearchAdapter(emptyList()) { clickedItem ->
            navigateToFormulas(clickedItem)
        }
        recyclerView.adapter = searchAdapter

        // 🔹 Garante que inicia invisível
        recyclerView.visibility = View.GONE

        val searchView = view.findViewById<androidx.appcompat.widget.SearchView>(R.id.search_view)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                filterContent(newText)
                return true
            }
        })
    }

    /**
     * Lê todos os arquivos .json da pasta assets, converte-os de dados complexos (Subjects)
     * para uma lista simples e otimizada (SearchableItem) para a busca.
     */
    private fun loadAndTransformContentFromAssets() {
        searchableList.clear() // Garante que a lista esteja vazia antes de carregar

        try {
            // Lista todos os arquivos na raiz da pasta assets
            val fileNames = requireContext().assets.list("")

            fileNames?.forEach { fileName ->
                if (fileName.endsWith(".json")) {
                    // Usa o DisciplinaJsonReader para carregar o objeto
                    val subject = disciplinaReader.loadDisciplina(requireContext(), fileName)

                    if (subject != null) {
                        /* A MÁGICA ACONTECE AQUI - apenas aceite */
                        subject.formulas?.forEach { formula ->
                            val searchText = (
                                    subject.name + " " +
                                            formula.name + " " +
                                            formula.description + " " +
                                            (subject.tags?.joinToString(" ") ?: "") + " " +
                                            (subject.alias?.joinToString(" ") ?: "")
                                    ).lowercase(Locale.ROOT)

                            searchableList.add(
                                SearchableItem(
                                    title = formula.name,
                                    description = subject.name,
                                    searchText = searchText,
                                    sourceFile = fileName
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("HomeTab", "Erro geral ao carregar assets", e)
        }
    }

    /** Filtra a lista de busca com base no texto digitado pelo usuário. */
    private fun filterContent(query: String?) {
        val recyclerView = view?.findViewById<RecyclerView>(R.id.recycler_view_results)

        if (query.isNullOrBlank()) {
            searchAdapter.updateList(emptyList())
            recyclerView?.visibility = View.GONE   // 🔹 esconde completamente
            return
        }

        val filteredList = searchableList.filter { item ->
            item.searchText.contains(query.lowercase(Locale.ROOT))
        }

        searchAdapter.updateList(filteredList)

        recyclerView?.visibility = if (filteredList.isEmpty()) View.GONE else View.VISIBLE
    }


    /**
     * Navega para a FormulasActivity quando um item da busca é clicado.
     * ESTA FUNÇÃO AGORA ESTÁ DENTRO DA CLASSE.
     */
    private fun navigateToFormulas(item: SearchableItem) {
        Log.d("HomeTab_Navigation", "Clicou em '${item.title}', navegando com o arquivo: ${item.sourceFile}")
        try {
            val intent = Intent(requireContext(), FormulasActivity::class.java).apply {
                putExtra("disciplina_arquivo_json", item.sourceFile)
                putExtra("disciplina_nome", item.description)
                putExtra("formula_nome_foco", item.title)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("HomeTab_Navigation", "Erro ao iniciar FormulasActivity", e)
            Toast.makeText(requireContext(), "Não foi possível abrir a disciplina.", Toast.LENGTH_SHORT).show()
        }
    }
}