package com.example.myapplication

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import kotlin.jvm.java

class SplashActivity : AppCompatActivity() {

    // O tempo de delay pode ser ajustado conforme a duração da sua animação
    private val SPLASH_DELAY: Long = 2500 // 2.5 segundos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val appTitle: TextView = findViewById(R.id.app_intro)

        // Animação (seu código de animação está perfeito, mantemos ele)
        appTitle.alpha = 0f
        appTitle.translationX = -200f
        appTitle.animate()
            .alpha(1f)
            .translationX(0f)
            .setDuration(1500)
            .start()

        // Lógica de navegação
        Handler(Looper.getMainLooper()).postDelayed({
            // Acessa as preferências
            val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)

            // Apenas LÊ o valor. O padrão é 'true' se a chave não existir.
            // Vamos usar uma chave mais clara: "onboarding_completed"
            val onboardingCompleted = sharedPref.getBoolean("onboarding_completed", false)

            // Decide para qual tela ir
            val intent = if (onboardingCompleted) {
                // Se JÁ FOI COMPLETADO, vai para a tela principal
                Intent(this, MainActivity::class.java)
            } else {
                // Se NÃO FOI COMPLETADO (ou é a primeira vez), vai para o onboarding
                Intent(this, OnboardingActivity::class.java)
            }

            startActivity(intent)
            finish() // Fecha a SplashActivity

        }, SPLASH_DELAY)
    }
}