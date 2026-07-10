package com.wb.example.demo_02

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    // 用 Dispatchers.Default，避免依赖 Dispatchers.Main（需 kotlinx-coroutines-android）
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 跑 Kotlin 基础练习，结果输出到 Logcat（Tag: KotlinDemo）
        KotlinPlayground.runAll()

        // 跑协程并发练习（Week1 检查点：并发拉两个接口并合并）
        scope.launch {
            val result = CoroutineExercises.runConcurrencyDemo()
            Log.d("KotlinDemo", "协程结果 -> $result")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel() // Activity 销毁后必须取消协程，否则泄漏
    }
}
