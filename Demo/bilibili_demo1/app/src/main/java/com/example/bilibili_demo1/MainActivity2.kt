package com.example.bilibili_demo1

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bilibili_demo1.databinding.ActivityMain2Binding

class MainActivity2 : AppCompatActivity() {

    private lateinit var binding: ActivityMain2Binding

    private val screenPadding by lazy {
        resources.getDimensionPixelSize(R.dimen.screen_padding)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // 与 MainActivity 保持一致：基础 padding + 系统栏高度，避免状态栏/导航栏遮挡
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                screenPadding + systemBars.left,
                screenPadding + systemBars.top,
                screenPadding + systemBars.right,
                screenPadding + systemBars.bottom
            )
            insets
        }
    }
}
