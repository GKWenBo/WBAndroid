package com.example.learnkotlin

import org.junit.Test

class NullSaveTest {

    @Test
    fun main() {
        val a: String = "hi"     // 非空,不能赋 null
        val b: String? = null    // 可空,才能赋 null
// a = null
    }

    fun demo(x: String?) {
        // 智能转换:等价于 Swift 的 if let
        if (x != null) {
            println(x.length)   // 这里 x 是 String,不用 ?
        }

        // 等价于 Swift 的 guard let
        val safe = x ?: return
        println(safe.length)    // safe 是 String
    }

    @Test
    fun test() {
        val name: String? = "WenBo"

// it 是解包后的非空值(默认名 it,也可自己命名)
        name?.let {
            println("Hello, $it")   // 只有 name 非空才执行
        }

// 命名参数版本
        name?.let { n ->
            println(n.uppercase())
        }

// 配合 Elvis 处理 null 分支 = if-let-else
        val len = name?.let { it.length } ?: 0
    }

    @Test
    fun test1() {
        var name: String? = "WenBo"

        name = null
        println(name ?: "aa")

        name = name?.let { it.capitalize() } ?: "Butterfly"
        println(name)
    }

}