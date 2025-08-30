package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button // IMPORTANTE: Para os botões de filtro
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
// import android.content.pm.PackageManager // Não usado diretamente nesta versão dos filtros
import com.example.myapplication.models.Desenvolvedor
import com.example.myapplication.models.TipoDesenvolvedor

class DevsTab : Fragment(), DevActionsListener {

    private lateinit var recyclerViewDesenvolvedores: RecyclerView
    private lateinit var devAdapter: DesenvolvedorAdapter

    // MODIFICAÇÃO: Lista para guardar TODOS os desenvolvedores (a fonte da verdade)
    private var listaCompletaDevs: List<Desenvolvedor> = listOf()
    // MODIFICAÇÃO: A lista que o adapter usará será uma lista mutável baseada na filtragem
    private var listaExibidaDevs: MutableList<Desenvolvedor> = mutableListOf()

    // DECLARAÇÃO DOS BOTÕES DE FILTRO (ajuste os tipos se não forem Button padrão)
    private lateinit var btnFiltroTodos: Button
    private lateinit var btnFiltroAlunos: Button
    private lateinit var btnFiltroProfessores: Button


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.tab_devs, container, false)

        // Inicializar RecyclerView
        recyclerViewDesenvolvedores = view.findViewById(R.id.recycler_view_desenvolvedores)
        recyclerViewDesenvolvedores.layoutManager = LinearLayoutManager(requireContext())

        // Inicializar Adapter com a lista que será exibida (inicialmente vazia ou todos)
        devAdapter = DesenvolvedorAdapter(listaExibidaDevs, this)
        recyclerViewDesenvolvedores.adapter = devAdapter

        // --- INÍCIO: CONFIGURAÇÃO DOS BOTÕES DE FILTRO ---
        // Encontre os botões pelo ID (SUBSTITUA OS IDs PELOS SEUS IDs REAIS DO XML)
        btnFiltroTodos = view.findViewById(R.id.btn_filtro_todos) // Exemplo de ID
        btnFiltroAlunos = view.findViewById(R.id.btn_filtro_alunos) // Exemplo de ID
        btnFiltroProfessores = view.findViewById(R.id.btn_filtro_professores) // Exemplo de ID

        btnFiltroTodos.setOnClickListener {
            Log.d("DevsTab", "Botão TODOS clicado")
            aplicarFiltro(null) // null ou um tipo especial para "TODOS"
        }

        btnFiltroAlunos.setOnClickListener {
            Log.d("DevsTab", "Botão ALUNOS clicado")
            aplicarFiltro(TipoDesenvolvedor.ALUNO)
        }

        btnFiltroProfessores.setOnClickListener {
            Log.d("DevsTab", "Botão PROFESSORES clicado")
            aplicarFiltro(TipoDesenvolvedor.PROFESSOR)
        }
        // --- FIM: CONFIGURAÇÃO DOS BOTÕES DE FILTRO ---

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        carregarDesenvolvedoresOriginais() // Carrega a lista completa
        aplicarFiltro(null) // Exibe todos inicialmente
    }

    // Renomeada para indicar que carrega a lista original/completa
    private fun carregarDesenvolvedoresOriginais() {
        // Seus dados Hardcoded ou carregados de outra fonte
        val devsOriginais = listOf(
            Desenvolvedor("1", "Alexandre Nunes da Silva Filho", "ic_dev1", "Desenvolvedor Full-Stack", TipoDesenvolvedor.ALUNO, "xandyhsilvah@gmail.com", "https://github.com/ale1zin"),
            Desenvolvedor("2", "Carlos Alexandre Dutra Volz", "ic_dev2", "Desenvolvedor Full-Stack", TipoDesenvolvedor.ALUNO, null, "https://github.com/Carlosvolz"),
            Desenvolvedor("3", "Eduardo Peixoto Alves Decker", "ic_dev3", "Desenvolvedor Full-Stack", TipoDesenvolvedor.ALUNO, null, "https://github.com/eduardodecker2006"),
            Desenvolvedor("4", "Yuri Andrade dos Anjos", null, "Desenvolvedor Full-Stack", TipoDesenvolvedor.ALUNO, null, "https://github.com/YuriXbr"),
            Desenvolvedor("5", "Fabricio Neitzke Ferreira", "ic_devt1", "Professor Orientador", TipoDesenvolvedor.PROFESSOR, "fabricioferreira@ifsul.edu.br", null),
            Desenvolvedor("6", "Rodrigo Nuevo Lellis", "ic_devt2", "Professor Orientador", TipoDesenvolvedor.PROFESSOR, "rodrigolellis@ifsul.edu.br", null)
        )
        // Atualiza a lista completa
        listaCompletaDevs = devsOriginais
        Log.d("DevsTab", "Lista completa de desenvolvedores carregada: ${listaCompletaDevs.size} itens")
    }

    // NOVA FUNÇÃO PARA APLICAR O FILTRO E ATUALIZAR O ADAPTER
    private fun aplicarFiltro(tipo: TipoDesenvolvedor?) {
        val listaFiltrada: List<Desenvolvedor> = if (tipo == null) {
            // Se o tipo for null, mostra todos
            listaCompletaDevs
        } else {
            // Caso contrário, filtra pelo tipo especificado
            listaCompletaDevs.filter { it.tipo == tipo }
        }

        listaExibidaDevs.clear()
        listaExibidaDevs.addAll(listaFiltrada)

        // Log antes de notificar o adapter
        Log.d("DevsTab_Filtro", "--- Aplicando Filtro ---")
        Log.d("DevsTab_Filtro", "Tipo: $tipo")
        Log.d("DevsTab_Filtro", "Lista Exibida (Tamanho: ${listaExibidaDevs.size}):")
        for ((index, dev) in listaExibidaDevs.withIndex()) {
            Log.d("DevsTab_Filtro", "  [$index] Nome: ${dev.nome}, FotoURL: ${dev.fotoUrl}")
        }

        devAdapter.notifyDataSetChanged() // Notifica o adapter que os dados mudaram

        Log.d("DevsTab", "Filtro aplicado. Exibindo ${listaExibidaDevs.size} desenvolvedores.")
    }

    private fun fallbackEmailChooser(email: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, "Assunto do email")
        }

        try {
            Toast.makeText(requireContext(), "Escolha seu aplicativo de email", Toast.LENGTH_SHORT).show()
            val chooser = Intent.createChooser(intent, "Enviar email usando:")
            startActivity(chooser)
            Log.d("DevsTab", "AÇÃO: Chooser de email iniciado com sucesso")
        } catch (e: Exception) {
            Log.e("DevsTab", "ERRO: Falha ao iniciar chooser de email", e)
            Toast.makeText(requireContext(), "Nenhum aplicativo de email encontrado.", Toast.LENGTH_SHORT).show()
        }
    }

    // Implementações da Interface DevActionsListener (se estiver usando)
    override fun onEmailClick(email: String) {
        Log.d("DevsTab", "AÇÃO: onEmailClick recebido para email: $email")

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email?subject=")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            startActivity(intent)
            Log.d("DevsTab", "AÇÃO: EMAIL direto enviado")
        } catch (e: Exception) {
            Log.e("DevsTab", "ERRO: $e")
            // Fallback para sua solução que funciona
            fallbackEmailChooser(email)
        }
    }

    override fun onGithubClick(githubUrl: String) {
        Log.d("DevsTab", "GitHub click: $githubUrl")
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl))
        startActivity(intent)
    }
}

