package com.example.bilibili_demo1

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bilibili_demo1.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    // ViewBinding：由 Gradle 根据 res/layout/activity_main.xml 自动生成，替代 findViewById
    private lateinit var binding: ActivityMainBinding

    private val screenPadding by lazy {
        resources.getDimensionPixelSize(R.dimen.screen_padding)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 处理系统栏（状态栏 / 导航栏）遮挡：基础 padding + 系统栏高度
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

        binding.tvGreeting.text = getString(R.string.greeting)
        binding.btnAction.setOnClickListener {
            Toast.makeText(this, R.string.action_clicked, Toast.LENGTH_SHORT).show()
        }
        // 显式 Intent：this 作为 Context，MainActivity2::class.java 是目标 Activity 的 Class 对象
        binding.jump.setOnClickListener {
            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)
        }
    }
}
