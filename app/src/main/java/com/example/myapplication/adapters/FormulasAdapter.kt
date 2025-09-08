package com.example.myapplication.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceError // Necessário para WebResourceError
import android.webkit.WebResourceRequest // Necessário para WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.models.FormulaX
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

    // Função para converter textColorPrimary para hexadecimal
    private fun getTextColorPrimaryHex(): String {
        return try {
            val typedValue = TypedValue()
            val theme = context.theme

            // Resolve o atributo textColorPrimary do tema atual
            theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true)

            val colorInt = if (typedValue.type >= TypedValue.TYPE_FIRST_COLOR_INT &&
                typedValue.type <= TypedValue.TYPE_LAST_COLOR_INT) {
                // Se já é uma cor direta
                typedValue.data
            } else {
                // Se é uma referência, resolve ela
                ContextCompat.getColor(context, typedValue.resourceId)
            }

            // Converter para formato hexadecimal (#RRGGBB)
            String.format("#%06X", 0xFFFFFF and colorInt)

        } catch (e: Exception) {
            Log.e("FormulasAdapter", "Erro ao obter textColorPrimary: ${e.message}")
            // Fallback: detectar tema escuro/claro
            val nightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            when (nightMode) {
                Configuration.UI_MODE_NIGHT_YES -> "#FFFFFF" // Branco para tema escuro
                Configuration.UI_MODE_NIGHT_NO -> "#000000"  // Preto para tema claro
                else -> "#000000" // Default
            }
        }
    }

    inner class FormulaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val formulaName: TextView = view.findViewById(R.id.tv_formula_name)
        val formulaDescription: TextView = view.findViewById(R.id.tv_formula_description)
        val formulaWebView: WebView = view.findViewById(R.id.webview_latex_formula)
        val container: View = view

        private var isFormulaRendered = false
        private var isWebViewSetupDone = false

        init {
            setupWebViewDefaults(formulaWebView)
        }

        @SuppressLint("SetJavaScriptEnabled")
        private fun setupWebViewDefaults(webView: WebView) {
            if (isWebViewSetupDone) return
            webView.settings.javaScriptEnabled = true
            webView.settings.domStorageEnabled = true
            webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE

            // Define o fundo do WebView como transparente
            webView.setBackgroundColor(0x00000000)
            // Opcional: Para ajudar com a renderização da transparência em algumas versões/dispositivos
            // webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

            webView.webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                    consoleMessage?.let {
                        Log.d(
                            "KaTeX_WebView_JS",
                            "JS Console: \"${it.message()}\" -- (Source: ${it.sourceId()}, Line: ${it.lineNumber()})"
                        )
                    }
                    return true
                }
            }
            isWebViewSetupDone = true
        }

        fun bind(formula: FormulaX) {
            formulaName.text = formula.name
            formulaDescription.text = formula.description

            formulaWebView.isVisible = false
            isFormulaRendered = false

            container.setOnClickListener {
                onFormulaClick(formula)
                toggleFormulaDisplay(formula)
            }
        }

        private fun toggleFormulaDisplay(formula: FormulaX) {
            if (formulaWebView.isVisible) {
                formulaWebView.isVisible = false
            } else {
                formulaWebView.isVisible = true
                if (!isFormulaRendered && formula.latex.isNotEmpty()) {
                    renderFormulaInWebView(formula)
                }
            }
        }

        private fun renderFormulaInWebView(formula: FormulaX) {
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

                        // Obter a cor textColorPrimary em hexadecimal
                        val formulaTextColor = this@FormulasAdapter.getTextColorPrimaryHex()
                        val jsCommand = "javascript:clearFormula(); setPageTextColor('${formulaTextColor}'); displayFormula('${escapedLatex}');"
                        Log.d("KaTeX_JS_Command", "Executando JS: $jsCommand") // Para depuração
                        view?.evaluateJavascript(jsCommand, null)
                        isFormulaRendered = true
                    } else {
                        view?.evaluateJavascript("javascript:clearFormula();", null)
                        isFormulaRendered = false
                    }
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        if (request?.isForMainFrame == true) {
                            Log.e("KaTeX_WebView_Error", "Erro MainFrame: ${error?.errorCode} ${error?.description} para ${request.url}")
                            isFormulaRendered = false
                        }
                    } else {
                        Log.e("KaTeX_WebView_Error", "Erro WebView: ${error?.description} para ${request?.url}")
                    }
                }
            }
            formulaWebView.loadDataWithBaseURL("file:///android_asset/", htmlKatexTemplate, "text/html", "UTF-8", null)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FormulaViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_formula, parent, false)
        return FormulaViewHolder(view)
    }

    override fun onBindViewHolder(holder: FormulaViewHolder, position: Int) {
        val formula = formulas[position]
        holder.bind(formula)
    }

    override fun getItemCount() = formulas.size
}