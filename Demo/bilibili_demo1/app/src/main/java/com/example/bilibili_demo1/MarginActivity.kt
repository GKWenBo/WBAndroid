package com.example.bilibili_demo1

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bilibili_demo1.databinding.ActivityMarginBinding

class MarginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMarginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMarginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 本页用于演示 margin 与 padding 的区别，XML 里已写死 20dp margin + 60dp padding。
        // 先记下 XML 的基础 padding，insets 只在其上叠加系统栏高度——
        // 直接 setPadding(systemBars) 会把 60dp 覆盖掉，演示内容就没了。
        val baseLeft = binding.root.paddingLeft
        val baseTop = binding.root.paddingTop
        val baseRight = binding.root.paddingRight
        val baseBottom = binding.root.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                baseLeft + systemBars.left,
                baseTop + systemBars.top,
                baseRight + systemBars.right,
                baseBottom + systemBars.bottom
            )
            insets
        }
    }
}
