package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.models.Desenvolvedor
import com.example.myapplication.models.TipoDesenvolvedor

class DevsTab : Fragment(), DevActionsListener {

    private lateinit var recyclerViewDesenvolvedores: RecyclerView
    private lateinit var devAdapter: DesenvolvedorAdapter

    private var listaCompletaDevs: List<Desenvolvedor> = listOf()
    private var listaExibidaDevs: MutableList<Desenvolvedor> = mutableListOf()

    // Novos botões
    private lateinit var btnFiltroDropdown: Button
    private lateinit var btnColaboradores: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.tab_devs, container, false)

        // Inicializar RecyclerView
        recyclerViewDesenvolvedores = view.findViewById(R.id.recycler_view_desenvolvedores)
        recyclerViewDesenvolvedores.layoutManager = LinearLayoutManager(requireContext())

        // Inicializar Adapter
        devAdapter = DesenvolvedorAdapter(listaExibidaDevs, this)
        recyclerViewDesenvolvedores.adapter = devAdapter

        // Configurar novos botões
        btnFiltroDropdown = view.findViewById(R.id.btn_filtro_dropdown)
        btnColaboradores = view.findViewById(R.id.btn_colaboradores)

        // Configurar botão dropdown de filtros
        btnFiltroDropdown.setOnClickListener {
            mostrarMenuFiltro(it)
        }

        // Configurar botão de colaboradores
        btnColaboradores.setOnClickListener {
            abrirColaboradores()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        carregarDesenvolvedoresOriginais()
        aplicarFiltro(null) // Exibe todos inicialmente
    }

    private fun mostrarMenuFiltro(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.menu_filtro_devs, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_filtro_todos -> {
                    Log.d("DevsTab", "Filtro TODOS selecionado")
                    aplicarFiltro(null)
                    btnFiltroDropdown.text = "Todos"
                    true
                }
                R.id.menu_filtro_alunos -> {
                    Log.d("DevsTab", "Filtro ALUNOS selecionado")
                    aplicarFiltro(TipoDesenvolvedor.ALUNO)
                    btnFiltroDropdown.text = "Alunos"
                    true
                }
                R.id.menu_filtro_professores -> {
                    Log.d("DevsTab", "Filtro PROFESSORES selecionado")
                    aplicarFiltro(TipoDesenvolvedor.PROFESSOR)
                    btnFiltroDropdown.text = "Professores"
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun abrirColaboradores() {
        Log.d("DevsTab", "Abrindo tela de Colaboradores")

        // 1. Criar o Intent para a nova ColaboradoresActivity
        val intent = Intent(requireContext(), ColaboradoresActivity::class.java)

        // 2. Iniciar a Activity
        startActivity(intent)

        /* CÓDIGO ANTIGO DO FRAGMENTO (Removido porque tinha animações diferentes de "Activity" por padrão)
        parentFragmentManager.beginTransaction()
            .replace(android.R.id.content, fragment)
            .addToBackStack(null)
            .commit()
        */
    }

    private fun carregarDesenvolvedoresOriginais() {
        val devsOriginais = listOf(
            Desenvolvedor("1", "Alexandre Nunes da Silva Filho", "ic_dev1", "Desenvolvedor Full-Stack", TipoDesenvolvedor.ALUNO, "xandyhsilvah@gmail.com", "https://github.com/ale1zin", "https://www.linkedin.com/in/ale1zin/", "https://www.instagram.com/ale1zin/"),
            Desenvolvedor("2", "Carlos Alexandre Dutra Volz", "ic_dev2", "Desenvolvedor Full-Stack", TipoDesenvolvedor.ALUNO, null, "https://github.com/Carlosvolz", null, "https://www.instagram.com/carlos__volz/"),
            Desenvolvedor("3", "Eduardo Peixoto Alves Decker", "ic_dev3", "Desenvolvedor Full-Stack", TipoDesenvolvedor.ALUNO, null, "https://github.com/eduardodecker2006", null, "https://www.instagram.com/eduardopeixotoalves/"),
            Desenvolvedor("4", "Yuri Andrade dos Anjos", "ic_dev4", "Desenvolvedor Full-Stack", TipoDesenvolvedor.ALUNO, "yurixbroficial@gmail.com", "https://github.com/YuriXbr", "https://www.linkedin.com/in/yuri-andrade-dos-anjos-08a98027a/", null),
            Desenvolvedor("5", "Fabricio Neitzke Ferreira", "ic_devt1", "Professor Orientador", TipoDesenvolvedor.PROFESSOR, "fabricioferreira@ifsul.edu.br", null, null, null),
            Desenvolvedor("6", "Rodrigo Nuevo Lellis", "ic_devt2", "Professor Orientador", TipoDesenvolvedor.PROFESSOR, "rodrigolellis@ifsul.edu.br", null, null, "https://www.instagram.com/rodrigonuevolellis/")
        )
        listaCompletaDevs = devsOriginais
        Log.d("DevsTab", "Lista completa de desenvolvedores carregada: ${listaCompletaDevs.size} itens")
    }

    private fun aplicarFiltro(tipo: TipoDesenvolvedor?) {
        val listaFiltrada: List<Desenvolvedor> = if (tipo == null) {
            listaCompletaDevs
        } else {
            listaCompletaDevs.filter { it.tipo == tipo }
        }

        listaExibidaDevs.clear()
        listaExibidaDevs.addAll(listaFiltrada)

        Log.d("DevsTab_Filtro", "--- Aplicando Filtro ---")
        Log.d("DevsTab_Filtro", "Tipo: $tipo")
        Log.d("DevsTab_Filtro", "Lista Exibida (Tamanho: ${listaExibidaDevs.size}):")
        for ((index, dev) in listaExibidaDevs.withIndex()) {
            Log.d("DevsTab_Filtro", "  [$index] Nome: ${dev.nome}, FotoURL: ${dev.fotoUrl}")
        }

        devAdapter.notifyDataSetChanged()

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
            fallbackEmailChooser(email)
        }
    }

    override fun onGithubClick(githubUrl: String) {
        Log.d("DevsTab", "GitHub click: $githubUrl")
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl))
        startActivity(intent)
    }

    override fun onLinkedinClick(linkedinUrl: String) {
        Log.d("DevsTab", "LinkedIn click: $linkedinUrl")
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(linkedinUrl))
        startActivity(intent)
    }

    override fun onInstagramClick(instagramUrl: String) {
        Log.d("DevsTab", "Instagram click: $instagramUrl")
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(instagramUrl))
        startActivity(intent)
    }
}