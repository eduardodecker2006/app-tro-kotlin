package com.example.myapplication.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log // Necessário para android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage // Necessário para ConsoleMessage
import android.webkit.WebChromeClient // Necessário para WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.models.FormulaX // Certifique-se que o caminho está correto
import java.io.BufferedReader
import java.io.InputStreamReader

class FormulasAdapter(
    private val context: Context,
    private val formulas: List<FormulaX>,
    private val onFormulaClick: (FormulaX) -> Unit
) : RecyclerView.Adapter<FormulasAdapter.FormulaViewHolder>() {

    private val htmlKatexTemplate: String by lazy {
        loadHtmlFromAssets(context, "katex_renderer.html")
    }

    private fun loadHtmlFromAssets(context: Context, fileName: String): String {
        return try {
            val inputStream = context.assets.open(fileName)
            BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
        } catch (e: Exception) {
            e.printStackTrace()
            "<html><body>Erro ao carregar template KaTeX: ${e.message}</body></html>"
        }
    }

    inner class FormulaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val formulaName: TextView = view.findViewById(R.id.tv_formula_name)
        val formulaDescription: TextView = view.findViewById(R.id.tv_formula_description)
        val formulaWebView: WebView = view.findViewById(R.id.webview_latex_formula)
        val container: View = view

        init {
            setupWebViewDefaults(formulaWebView)
        }

        @SuppressLint("SetJavaScriptEnabled")
        private fun setupWebViewDefaults(webView: WebView) {
            webView.settings.javaScriptEnabled = true
            webView.settings.domStorageEnabled = true
            webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE

            // **** MODIFICAÇÃO ADICIONADA AQUI ****
            // Configura o WebChromeClient para capturar logs do console JavaScript
            webView.webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                    consoleMessage?.let {
                        val message = it.message()
                        val lineNumber = it.lineNumber()
                        val sourceId = it.sourceId()

                        Log.d(
                            "KaTeX_WebView_JS", // Tag para filtrar no Logcat
                            "JS Console: \"${message}\" -- (Source: ${sourceId}, Line: ${lineNumber})"
                        )
                    }
                    return true // Indica que a mensagem foi tratada
                }
            }
            // **** FIM DA MODIFICAÇÃO ****
        }

        fun bindFormula(formula: FormulaX, htmlTemplate: String) {
            formulaName.text = formula.name
            formulaDescription.text = formula.description
            container.setOnClickListener { onFormulaClick(formula) }

            formulaWebView.loadDataWithBaseURL("file:///android_asset/", htmlTemplate, "text/html", "UTF-8", null)

            formulaWebView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    if (formula.latex.isNotEmpty()) {
                        val latexToRender = formula.latex.joinToString(" \\\\\\\\ ")
                        val escapedLatex = latexToRender
                            .replace("\\", "\\\\")
                            .replace("'", "\\'")
                            .replace("\"", "\\\"")
                            .replace("\n", "\\n")
                        view?.evaluateJavascript("javascript:clearFormula(); displayFormula('${escapedLatex}');", null)
                    } else {
                        view?.evaluateJavascript("javascript:clearFormula();", null)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FormulaViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_formula, parent, false)
        return FormulaViewHolder(view)
    }

    override fun onBindViewHolder(holder: FormulaViewHolder, position: Int) {
        val formula = formulas[position]
        holder.bindFormula(formula, htmlKatexTemplate)
    }

    override fun getItemCount() = formulas.size
}
