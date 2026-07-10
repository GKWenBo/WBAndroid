package com.wb.example.demo_02

import android.util.Log

/**
 * Kotlin 基础练习（对照 Swift 工程师的已知心智）。
 * 每个函数演示一个核心语法点，结果打印到 Logcat（Tag: KotlinDemo）。
 * 在 Android Studio 里运行 App 后，打开 Logcat 过滤 "KotlinDemo" 即可看到输出。
 */
object KotlinPlayground {

    private const val TAG = "KotlinDemo"

    fun runAll() {
        variables()
        dataClassVsStruct()
        sealedClassVsEnum()
        extensionFunction()
        nullSafety()
        scopeFunctions()
        higherOrderAndLambda()
    }

    // 1) val/var ≈ let/var；默认参数 + 命名参数 ≈ Swift 默认参数
    private fun variables() {
        val name: String = "wenbo"   // val ≈ let（不可重赋）
        var age = 18                 // var ≈ var（可重赋）
        age = 19

        fun greet(title: String = "同学", target: String = name) =
            "Hello $title, I'm $target" // 字符串模板 ≈ "\(target)"

        Log.d(TAG, "1) variables: ${greet()} | ${greet(target = "iOSer")}")
    }

    // data class ≈ Swift struct（自动生成 equals/hashCode/toString/copy）
    private data class User(val id: Int, val name: String, val isVip: Boolean = false)

    private fun dataClassVsStruct() {
        val u1 = User(1, "A")
        val u2 = u1.copy(name = "B") // copy ≈ 改某个字段生成新实例
        Log.d(TAG, "2) data class: $u1 | copy -> $u2 | equal? ${u1 == User(1, "A")}")
    }

    // sealed class/interface ≈ Swift enum + 关联值，常用于表达 UI 状态机
    private sealed interface UiState {
        data object Loading : UiState
        data class Success(val data: List<String>) : UiState
        data class Error(val msg: String) : UiState
    }

    private fun sealedClassVsEnum() {
        val states: List<UiState> = listOf(
            UiState.Loading,
            UiState.Success(listOf("a", "b")),
            UiState.Error("网络异常")
        )
        states.forEach { state ->
            // when 覆盖全部分支时无需 else（编译器保证穷尽），≈ Swift 的 switch 必须 exhaustive
            val desc = when (state) {
                is UiState.Loading -> "加载中"
                is UiState.Success -> "成功 ${state.data.size} 条"
                is UiState.Error -> "失败: ${state.msg}"
            }
            Log.d(TAG, "3) sealed: $desc")
        }
    }

    // 扩展函数 ≈ Swift extension；可直接给已有类型加方法，无需继承
    private fun extensionFunction() {
        fun String.addExclaim() = "$this!"
        fun Int.square() = this * this
        Log.d(TAG, "4) extension: ${"Hi".addExclaim()} | 5^2=${5.square()}")
    }

    // ? 可空；?: Elvis 提供默认值；!! 强制解包（慎用，≈ Swift 的 !）
    private fun nullSafety() {
        var maybe: String? = null
        Log.d(TAG, "5) null: ${maybe?.length ?: 0}") // 安全调用 + Elvis 默认值
        maybe = "abc"
        Log.d(TAG, "5) null: ${maybe?.length}")       // 安全调用 ≈ Swift 的 ?.
    }

    // 作用域函数：let/run/apply/also/with，用来精简样板代码
    private fun scopeFunctions() {
        val user = User(2, "C").apply { /* 初始化 this */ }
        val len = user.name.let { it.length }         // it 是默认形参 ≈ Swift 尾随闭包 $0
        val desc = with(user) { "id=$id,name=$name" } // with 返回最后一行
        Log.d(TAG, "6) scope: len=$len | $desc")
    }

    // 高阶函数 + lambda；最后一个 lambda 可挪到括号外（尾随 lambda ≈ Swift）
    private fun higherOrderAndLambda() {
        val nums = listOf(1, 2, 3, 4)
        val doubled = nums.map { it * 2 }              // ≈ nums.map { $0 * 2 }
        val evenSum = nums.filter { it % 2 == 0 }.sum()
        Log.d(TAG, "7) lambda: doubled=$doubled | evenSum=$evenSum")
    }
}
