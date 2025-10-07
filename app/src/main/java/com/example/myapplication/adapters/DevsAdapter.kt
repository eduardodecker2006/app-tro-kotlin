package com.example.myapplication

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
// Glide Imports:
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.myapplication.R
import com.example.myapplication.models.Desenvolvedor

interface DevActionsListener {
    fun onEmailClick(email: String)
    fun onGithubClick(githubUrl: String)
    fun onLinkedinClick(linkedinUrl: String)
    fun onInstagramClick(instagramUrl: String)
}

class DesenvolvedorAdapter(
    private var desenvolvedores: List<Desenvolvedor>,
    private val listener: DevActionsListener? = null
) : RecyclerView.Adapter<DesenvolvedorAdapter.DesenvolvedorViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DesenvolvedorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_devs, parent, false)
        return DesenvolvedorViewHolder(view)
    }

    override fun onBindViewHolder(holder: DesenvolvedorViewHolder, position: Int) {
        val desenvolvedor = desenvolvedores[position]

        // LOG
        Log.d("DevAdapter_onBind", "Pos: $position, Nome: ${desenvolvedor.nome}, FotoString: ${desenvolvedor.fotoUrl}, Tipo: ${desenvolvedor.tipo}")

        holder.bind(desenvolvedor, listener)
    }

    override fun getItemCount(): Int = desenvolvedores.size

    fun updateData(newDesenvolvedores: List<Desenvolvedor>) {
        this.desenvolvedores = newDesenvolvedores
        notifyDataSetChanged()
    }

    class DesenvolvedorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgFoto: ImageView = itemView.findViewById(R.id.img_desenvolvedor_foto)
        private val tvNome: TextView = itemView.findViewById(R.id.tv_desenvolvedor_nome)
        private val tvFuncao: TextView = itemView.findViewById(R.id.tv_desenvolvedor_funcao)
        private val btnEmail: ImageButton = itemView.findViewById(R.id.btn_desenvolvedor_email)
        private val btnGithub: ImageButton = itemView.findViewById(R.id.btn_desenvolvedor_github)
        private val btnLinkedin: ImageButton = itemView.findViewById(R.id.btn_desenvolvedor_linkedin)
        private val btnInstagram: ImageButton = itemView.findViewById(R.id.btn_desenvolvedor_instagram)

        fun bind(desenvolvedor: Desenvolvedor, listener: DevActionsListener?) {
            // LOG
            Log.d("DevAdapter_bind", "Binding - Nome: ${desenvolvedor.nome}, FotoString: ${desenvolvedor.fotoUrl}")

            tvNome.text = desenvolvedor.nome
            tvFuncao.text = desenvolvedor.funcao

            val fotoUrl = desenvolvedor.fotoUrl
            val placeholderDrawable = R.drawable.ic_devdefault // Seu drawable padrão

            // CORREÇÃO PRINCIPAL: Cancelar qualquer carregamento anterior do Glide
            Glide.with(itemView.context).clear(imgFoto)

            // CORREÇÃO: Definir imagem placeholder imediatamente para evitar "fantasmas"
            imgFoto.setImageResource(placeholderDrawable)

            if (fotoUrl != null && fotoUrl.isNotBlank()) {
                val requestOptions = RequestOptions()
                    .placeholder(placeholderDrawable)
                    .error(placeholderDrawable)
                    .dontAnimate() // CORREÇÃO: Desabilitar animações para evitar conflitos

                if (fotoUrl.startsWith("http://") || fotoUrl.startsWith("https://")) {
                    // Carregar de URL da Internet
                    Glide.with(itemView.context)
                        .load(fotoUrl)
                        .apply(requestOptions)
                        .into(imgFoto)
                } else {
                    // NÃO é uma URL HTTP/HTTPS, então assumimos que é um nome de drawable
                    // Tentar carregar como um recurso drawable local
                    val imageResId = itemView.context.resources.getIdentifier(
                        fotoUrl, // O nome do arquivo sem extensão (ex: "ic_dev1")
                        "drawable",
                        itemView.context.packageName
                    )
                    if (imageResId != 0) {
                        // Recurso drawable encontrado!
                        // CORREÇÃO: Usar setImageResource diretamente para recursos locais
                        // em vez do Glide para evitar problemas de cache
                        imgFoto.setImageResource(imageResId)
                        Log.d("DevAdapter", "Drawable carregado diretamente: $fotoUrl para ${desenvolvedor.nome}")
                    } else {
                        // Recurso drawable NÃO encontrado com esse nome
                        Log.w("DevAdapter", "Drawable NÃO encontrado para: $fotoUrl. Usando default.")
                        imgFoto.setImageResource(placeholderDrawable)
                    }
                }
            } else {
                // fotoUrl é nula ou vazia
                Log.d("DevAdapter", "fotoUrl é nula ou vazia para ${desenvolvedor.nome}. Usando default.")
                imgFoto.setImageResource(placeholderDrawable)
            }

            // Configuração dos botões

            // Botão do Email
            if (desenvolvedor.email != null && desenvolvedor.email.isNotBlank()) {
                btnEmail.visibility = View.VISIBLE
                btnEmail.setOnClickListener {
                    Log.d("DevAdapter", "CLIQUE: Botão de Email para: ${desenvolvedor.email}")
                    listener?.onEmailClick(desenvolvedor.email)
                }
            } else {
                btnEmail.visibility = View.GONE
            }

            // Botão do GitHub
            if (desenvolvedor.githubUrl != null) {
                btnGithub.visibility = View.VISIBLE
                btnGithub.setOnClickListener { listener?.onGithubClick(desenvolvedor.githubUrl) }
            } else {
                btnGithub.visibility = View.GONE
            }

            // Botão do LinkedIn
            if (desenvolvedor.linkedinUrl != null) {
                btnLinkedin.visibility = View.VISIBLE
                btnLinkedin.setOnClickListener { listener?.onLinkedinClick(desenvolvedor.linkedinUrl) }
            } else {
                btnLinkedin.visibility = View.GONE
            }

            // Botão do Instagram
            if (desenvolvedor.instagramUrl != null) {
                btnInstagram.visibility = View.VISIBLE
                btnInstagram.setOnClickListener { listener?.onInstagramClick(desenvolvedor.instagramUrl) }
            } else {
                btnInstagram.visibility = View.GONE
            }
        }
    }
}