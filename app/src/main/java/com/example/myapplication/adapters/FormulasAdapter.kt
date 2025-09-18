package com.example.myapplication.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.models.Constants // Importa o typealias
import com.example.myapplication.models.FormulaX
import com.example.myapplication.models.Variables // Importa o typealias
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.text.append
import kotlin.text.forEach
import kotlin.text.isNotEmpty

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

    private fun getTextColorPrimaryHex(): String {
        return try {
            val typedValue = TypedValue()
            val theme = context.theme
            theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true)
            val colorInt = if (typedValue.type >= TypedValue.TYPE_FIRST_COLOR_INT &&
                typedValue.type <= TypedValue.TYPE_LAST_COLOR_INT) {
                typedValue.data
            } else {
                ContextCompat.getColor(context, typedValue.resourceId)
            }
            String.format("#%06X", 0xFFFFFF and colorInt)
        } catch (e: Exception) {
            Log.e("FormulasAdapter", "Erro ao obter textColorPrimary: ${e.message}")
            val nightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            when (nightMode) {
                Configuration.UI_MODE_NIGHT_YES -> "#FFFFFF"
                Configuration.UI_MODE_NIGHT_NO -> "#000000"
                else -> "#000000"
            }
        }
    }

    inner class FormulaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val formulaName: TextView = view.findViewById(R.id.tv_formula_name)
        val formulaDescription: TextView = view.findViewById(R.id.tv_formula_description)
        val formulaWebView: WebView = view.findViewById(R.id.webview_latex_formula)
        val container: View = itemView // itemView é o CardView

        private val expandableContentLayout: LinearLayout = itemView.findViewById(R.id.layout_expandable_content)
        private val expandStatusTextView: TextView = itemView.findViewById(R.id.tv_expand_status)
        val formulaVariablesDisplayTextView: TextView = view.findViewById(R.id.tv_formula_variables_display)

        private var isExpanded = false
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
            webView.setBackgroundColor(0x00000000)
            // webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null) // Opcional

            webView.webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                    consoleMessage?.let {
                        Log.d("KaTeX_WebView_JS", "JS Console: \"${it.message()}\" -- (Source: ${it.sourceId()}, Line: ${it.lineNumber()})")
                    }
                    return true
                }
            }
            isWebViewSetupDone = true
        }

        fun bind(formula: FormulaX) {
            formulaName.text = formula.name
            formulaDescription.text = formula.description

            val termsToDisplay = formatPhysicalTermsForDisplay(formula.variables, formula.constants)
            if (termsToDisplay.isNotBlank()) {
                formulaVariablesDisplayTextView.text = termsToDisplay
            } else {
                formulaVariablesDisplayTextView.text = ""
                formulaVariablesDisplayTextView.visibility = View.GONE
            }

            isFormulaRendered = false
            // A visibilidade inicial dos componentes dentro de expandableContentLayout
            // será gerenciada por updateExpandCollapseUI().
            // Se o estado padrão é não expandido, eles começarão como GONE.
            updateExpandCollapseUI()

            container.setOnClickListener {
                isExpanded = !isExpanded
                updateExpandCollapseUI()
                onFormulaClick(formula)

                if (isExpanded && !isFormulaRendered && formula.latex.isNotEmpty()) {
                    renderFormulaInWebView(formula)
                }
            }
        }

        private fun updateExpandCollapseUI() {
            if (isExpanded) {
                expandableContentLayout.visibility = View.VISIBLE
                formulaWebView.visibility = View.VISIBLE // Mostrar WebView da fórmula
                // Mostrar TextView das variáveis apenas se tiver conteúdo
                formulaVariablesDisplayTextView.visibility = if (formulaVariablesDisplayTextView.text.isNotBlank()) View.VISIBLE else View.GONE

                expandStatusTextView.text = "Recolher"
                expandStatusTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_up, 0)
            } else {
                expandableContentLayout.visibility = View.GONE
                // Não é estritamente necessário esconder os filhos aqui, pois o pai já está GONE,
                // mas não faz mal e garante o estado.
                formulaWebView.visibility = View.GONE
                formulaVariablesDisplayTextView.visibility = View.GONE

                expandStatusTextView.text = "Expandir"
                expandStatusTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0)
            }
        }

        private fun formatPhysicalTermsForDisplay(
            variablesMap: Variables?, // Variables é Map<String, String>?
            constantsMap: Constants?  // Constants é Map<String, String>?
        ): SpannableStringBuilder {
            val builder = SpannableStringBuilder()

            variablesMap?.forEach { (symbol, description) ->
                if (builder.isNotEmpty()) builder.append("\n")
                val start = builder.length
                builder.append("$symbol: ") // Símbolo da grandeza
                builder.setSpan(StyleSpan(Typeface.BOLD), start, builder.length, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
                builder.append(description) // Descrição da grandeza
            }

            if (constantsMap?.isNotEmpty() == true && variablesMap?.isNotEmpty() == true) {
                if (builder.isNotEmpty()) builder.append("\n\n") // Mais espaço antes das constantes
            }

            constantsMap?.forEach { (symbol, description) ->
                if (builder.isNotEmpty() && !builder.endsWith("\n\n") && !builder.endsWith("\n")) builder.append("\n")
                val start = builder.length
                // Você pode querer diferenciar constantes, por exemplo:
                // builder.append("(Constante) $symbol: ")
                builder.append("$symbol: ")
                builder.setSpan(StyleSpan(Typeface.BOLD), start, builder.length, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
                builder.append(description)
            }
            return builder
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
                        val formulaTextColor = this@FormulasAdapter.getTextColorPrimaryHex()
                        val jsCommand = "javascript:clearFormula(); setPageTextColor('${formulaTextColor}'); displayFormula('${escapedLatex}');"
                        Log.d("KaTeX_JS_Command", "Executando JS: $jsCommand")
                        view?.evaluateJavascript(jsCommand, null)
                        isFormulaRendered = true
                    } else {
                        view?.evaluateJavascript("javascript:clearFormula();", null)
                        isFormulaRendered = false
                    }
                }

                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
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
