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
    private lateinit var adapter: TabsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        viewPager = findViewById(R.id.view_pager)
        tabLayout = findViewById(R.id.tab_layout)

        adapter = TabsAdapter(this)

        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> { tab.text = "Início"; tab.icon = ContextCompat.getDrawable(this, R.drawable.ic_home) }
                1 -> { tab.text = "Disciplinas"; tab.icon = ContextCompat.getDrawable(this, R.drawable.ic_subjetcs) }
                2 -> { tab.text = "Resistores"; tab.icon = ContextCompat.getDrawable(this, R.drawable.ic_resistor) }
                3 -> { tab.text = "Devs"; tab.icon = ContextCompat.getDrawable(this, R.drawable.ic_devs) }
            }
        }.attach()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
