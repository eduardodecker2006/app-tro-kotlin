package com.example.myapplication

import android.graphics.drawable.VectorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import kotlin.math.pow

class ResistorTab : Fragment() {

    // Views do layout
    private lateinit var band1Image: ImageView
    private lateinit var band2Image: ImageView
    private lateinit var band3Image: ImageView
    private lateinit var band4Image: ImageView
    private lateinit var resistanceValue: TextView
    private lateinit var validationIcon: ImageView

    // Estado atual das faixas (valores -1 indicam não selecionado)
    private var firstBand = -1
    private var secondBand = -1
    private var multiplier = -1
    private var tolerance = -1

    // Mapeamento de cores para valores RGB
    private val colorMap = mapOf(
        0 to R.color.black,
        1 to R.color.brown,
        2 to R.color.red,
        3 to R.color.orange,
        4 to R.color.yellow,
        5 to R.color.green,
        6 to R.color.blue,
        7 to R.color.violet,
        8 to R.color.gray,
        9 to R.color.white,
        10 to R.color.golden,
        11 to R.color.silver
    )

    // Valores padrão da série E12
    private val e12Values = listOf(
        1.0, 1.2, 1.5, 1.8, 2.2, 2.7, 3.3, 3.9, 4.7, 5.6, 6.8, 8.2
    )

    // Multiplicadores para cada posição
    private val multipliers = mapOf(
        0 to 1.0,           // x1
        1 to 10.0,          // x10
        2 to 100.0,         // x100
        3 to 1000.0,        // x10³
        4 to 10000.0,       // x10⁴
        5 to 100000.0,      // x10⁵
        6 to 1000000.0,     // x10⁶
        7 to 10000000.0,    // x10⁷
        8 to 100000000.0,   // x10⁸
        9 to 1000000000.0,  // x10⁹
        10 to 0.1,          // x0.1
        11 to 0.01          // x0.01
    )

    // Tolerâncias
    private val tolerances = mapOf(
        1 to 1.0,      // ±1%
        2 to 2.0,      // ±2%
        5 to 0.5,      // ±0.5%
        6 to 0.25,     // ±0.25%
        7 to 0.1,      // ±0.1%
        8 to 0.05,     // ±0.05%
        10 to 5.0,     // ±5%
        11 to 10.0     // ±10%
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.tab_resistor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar views
        band1Image = view.findViewById(R.id.band1_image)
        band2Image = view.findViewById(R.id.band2_image)
        band3Image = view.findViewById(R.id.band3_image)
        band4Image = view.findViewById(R.id.band4_image)
        resistanceValue = view.findViewById(R.id.resistance_value)
        validationIcon = view.findViewById(R.id.validation_icon)

        setupButtons(view)

        // Definir valores padrão para resistor de 1kΩ (marrom, preto, vermelho, dourado)
        setDefaultResistor()
    }

    private fun setDefaultResistor() {
        // Resistor de 1kΩ:
        // Faixa 1: Marrom (1)
        // Faixa 2: Preto (0)
        // Faixa 3: Vermelho (x100)
        // Faixa 4: Dourado (±5%)

        firstBand = 1    // Marrom
        secondBand = 0   // Preto
        multiplier = 2   // Vermelho (x100)
        tolerance = 10   // Dourado (±5%)

        updateResistorDisplay()
        calculateAndDisplayValue()
    }

    private fun setupButtons(view: View) {
        // Configurar botões da primeira coluna (primeiro dígito)
        for (i in 0..9) {
            val buttonId = resources.getIdentifier("btn_c1_$i", "id", requireContext().packageName)
            val button = view.findViewById<MaterialButton>(buttonId)
            button?.setOnClickListener { selectFirstBand(i) }
        }

        // Configurar botões da segunda coluna (segundo dígito)
        for (i in 0..9) {
            val buttonId = resources.getIdentifier("btn_c2_$i", "id", requireContext().packageName)
            val button = view.findViewById<MaterialButton>(buttonId)
            button?.setOnClickListener { selectSecondBand(i) }
        }

        // Configurar botões da terceira coluna (multiplicador)
        for (i in 0..11) {
            val buttonId = resources.getIdentifier("btn_c3_$i", "id", requireContext().packageName)
            val button = view.findViewById<MaterialButton>(buttonId)
            button?.setOnClickListener { selectMultiplier(i) }
        }

        // Configurar botões da quarta coluna (tolerância)
        val toleranceButtons = listOf(1, 2, 5, 6, 7, 8, 10, 11)
        for (i in toleranceButtons) {
            val buttonId = resources.getIdentifier("btn_c4_$i", "id", requireContext().packageName)
            val button = view.findViewById<MaterialButton>(buttonId)
            button?.setOnClickListener { selectTolerance(i) }
        }
    }

    private fun selectFirstBand(value: Int) {
        firstBand = value
        updateResistorDisplay()
        calculateAndDisplayValue()
    }

    private fun selectSecondBand(value: Int) {
        secondBand = value
        updateResistorDisplay()
        calculateAndDisplayValue()
    }

    private fun selectMultiplier(value: Int) {
        multiplier = value
        updateResistorDisplay()
        calculateAndDisplayValue()
    }

    private fun selectTolerance(value: Int) {
        tolerance = value
        updateResistorDisplay()
        calculateAndDisplayValue()
    }

    private fun updateResistorDisplay() {
        // Atualizar a cor da primeira faixa
        firstBand.takeIf { it != -1 }?.let { value ->
            val color = ContextCompat.getColor(requireContext(), getColorForValue(value))
            band1Image.setColorFilter(color)
        }

        // Atualizar a cor da segunda faixa
        secondBand.takeIf { it != -1 }?.let { value ->
            val color = ContextCompat.getColor(requireContext(), getColorForValue(value))
            band2Image.setColorFilter(color)
        }

        // Atualizar a cor do multiplicador
        multiplier.takeIf { it != -1 }?.let { value ->
            val color = ContextCompat.getColor(requireContext(), getColorForValue(value))
            band3Image.setColorFilter(color)
        }

        // Atualizar a cor da tolerância
        tolerance.takeIf { it != -1 }?.let { value ->
            val color = ContextCompat.getColor(requireContext(), getColorForValue(value))
            band4Image.setColorFilter(color)
        }
    }

    private fun calculateAndDisplayValue() {
        if (firstBand == -1 || secondBand == -1 || multiplier == -1) {
            resistanceValue.text = "--- Ω"
            validationIcon.visibility = View.GONE
            return
        }

        val baseValue = (firstBand * 10 + secondBand).toDouble()
        val multiplierValue = multipliers[multiplier] ?: 1.0
        val finalValue = baseValue * multiplierValue

        // Formatação do valor com tolerância
        val formattedValue = formatResistanceValue(finalValue)
        val toleranceText = if (tolerance != -1) {
            " (±${tolerances[tolerance]}%)"
        } else {
            ""
        }
        resistanceValue.text = "$formattedValue$toleranceText"

        // Validação E12
        val isValidE12 = validateE12(finalValue)
        
        // Sempre mostrar o ícone de validação
        validationIcon.visibility = View.VISIBLE
        
        // Atualizar ícone de validação
        if (isValidE12) {
            validationIcon.setImageResource(R.drawable.ic_check)
            validationIcon.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
            // Opcional: Adicionar descrição para acessibilidade
            validationIcon.contentDescription = "Valor válido na série E12"
        } else {
            validationIcon.setImageResource(R.drawable.ic_error)
            validationIcon.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
            // Opcional: Adicionar descrição para acessibilidade
            validationIcon.contentDescription = "Valor não pertence à série E12"
        }

        // Opcional: Adicionar animação suave na troca dos ícones
        validationIcon.alpha = 0f
        validationIcon.animate()
            .alpha(1f)
            .setDuration(300)
            .start()
    }

    private fun formatResistanceValue(value: Double): String {
        // Adiciona uma verificação específica para o valor 0.0
        if (value == 0.0) {
            return String.format("%.2f Ω", 0.0) // Retorna "0.00 Ω"
        }

        return when {
            value >= 1_000_000_000 -> String.format("%.2f GΩ", value / 1_000_000_000)
            value >= 1_000_000 -> String.format("%.2f MΩ", value / 1_000_000)
            value >= 1_000 -> String.format("%.2f kΩ", value / 1_000)
            value >= 1 -> String.format("%.2f Ω", value)
            value >= 0.001 -> String.format("%.2f mΩ", value * 1_000)
            else -> String.format("%.2e Ω", value) // Agora só para valores muito pequenos (não-zero)
        }
    }

    private fun validateE12(resistance: Double): Boolean {
        if (resistance <= 0) return false

        // Encontrar a potência de 10 mais próxima
        val exponent = Math.floor(Math.log10(resistance))
        val decade = Math.pow(10.0, exponent)
        
        // Normalizar o valor para o intervalo [1, 10)
        val normalizedValue = resistance / decade

        // Tolerância padrão para comparação (mais restrita para maior precisão)
        val tolerance = 0.005 // 0.5% de tolerância para comparação

        // Verificar se o valor normalizado está próximo de algum valor E12
        val isValid = e12Values.any { e12Value ->
            val relativeError = Math.abs(normalizedValue - e12Value) / e12Value
            relativeError <= tolerance
        }

        return isValid
    }

    // Função auxiliar para obter o valor E12 mais próximo (opcional, pode ser útil para feedback)
    private fun getNearestE12Value(resistance: Double): Double {
        if (resistance <= 0) return 0.0

        val exponent = Math.floor(Math.log10(resistance))
        val decade = Math.pow(10.0, exponent)
        val normalizedValue = resistance / decade

        // Encontrar o valor E12 mais próximo
        val nearestE12 = e12Values.minByOrNull { e12Value ->
            Math.abs(Math.log10(normalizedValue) - Math.log10(e12Value))
        } ?: 1.0

        return nearestE12 * decade
    }

    // Função auxiliar para obter cor por valor
    private fun getColorForValue(value: Int): Int {
        return colorMap[value] ?: R.color.black
    }

    // Função para resetar todas as seleções
    fun resetCalculator() {
        firstBand = -1
        secondBand = -1
        multiplier = -1
        tolerance = -1
        resistanceValue.text = "--- Ω"
        validationIcon.visibility = View.GONE
        updateResistorDisplay()
    }

    // Função para obter informações detalhadas sobre o resistor atual
    fun getResistorInfo(): String {
        if (firstBand == -1 || secondBand == -1 || multiplier == -1) {
            return "Resistor incompleto"
        }

        val baseValue = (firstBand * 10 + secondBand).toDouble()
        val multiplierValue = multipliers[multiplier] ?: 1.0
        val finalValue = baseValue * multiplierValue
        val toleranceValue = if (tolerance != -1) tolerances[tolerance] else null

        val info = StringBuilder()
        info.append("Valor: ${formatResistanceValue(finalValue)}\n")
        info.append("Primeira faixa: $firstBand\n")
        info.append("Segunda faixa: $secondBand\n")
        info.append("Multiplicador: ${getMultiplierText(multiplier)}\n")

        if (toleranceValue != null) {
            info.append("Tolerância: ±${toleranceValue}%\n")
        }

        info.append("E12 válido: ${if (validateE12(finalValue)) "Sim" else "Não"}")

        return info.toString()
    }

    private fun getMultiplierText(multiplierIndex: Int): String {
        return when (multiplierIndex) {
            0 -> "x1"
            1 -> "x10"
            2 -> "x100"
            3 -> "x10³"
            4 -> "x10⁴"
            5 -> "x10⁵"
            6 -> "x10⁶"
            7 -> "x10⁷"
            8 -> "x10⁸"
            9 -> "x10⁹"
            10 -> "x0.1"
            11 -> "x0.01"
            else -> "x1"
        }
    }
}