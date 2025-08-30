package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2 // Importar ViewPager2
import com.example.myapplication.adapters.TabsAdapter
import com.google.android.material.tabs.TabLayout // Importar TabLayout
import com.google.android.material.tabs.TabLayoutMediator // Importar TabLayoutMediator

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var adapter: TabsAdapter // Nosso adaptador

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge() // Você pode manter ou remover conforme sua necessidade de UI
        setContentView(R.layout.activity_main)

        // 1. Encontrar as Views do layout pelo ID
        viewPager = findViewById(R.id.view_pager)
        tabLayout = findViewById(R.id.tab_layout)

        // 2. Criar uma instância do nosso TabsPagerAdapter
        // 'this' refere-se à MainActivity, que é uma FragmentActivity
        adapter = TabsAdapter(this)

        // 3. Definir o adapter para o ViewPager2
        viewPager.adapter = adapter

        // 4. Conectar o TabLayout com o ViewPager2 usando TabLayoutMediator
        // Isso também define os títulos das abas
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "Início" // Título do "botão" da 1° aba
                    tab.icon = ContextCompat.getDrawable(this, R.drawable.ic_home) // Ícone home
                }
                1 -> {
                    tab.text = "Disciplinas" // Título do "botão" da 2° aba
                    tab.icon = ContextCompat.getDrawable(this, R.drawable.ic_subjetcs) // Defina o ícone
                }
                2 -> {
                    tab.text = "Código de Cores" // Título do "botão" da 3° aba
                    tab.icon = ContextCompat.getDrawable(this, R.drawable.ic_resistor) // Defina o ícone
                }
                3 -> {
                    tab.text = "Desenvolvedores" // Título do "botão" da 4° aba
                    tab.icon = ContextCompat.getDrawable(this, R.drawable.ic_devs) // Defina o ícone
                }

            }
        }.attach() // Não se esqueça de chamar attach()!

        // Código para ajustar o padding para barras do sistema (opcional, pode manter)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
