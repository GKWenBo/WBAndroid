package com.example.learnkotlin

import org.junit.Test

class ForeachListTest {
    @Test
    fun main() {
        val list = listOf("1", "2", "3")
        for (s in list) {
            println(s)
        }

        list.forEach {
            it
            println(it)
        }

        list.forEachIndexed { index, item -> println("$index $item") }
    }

    /*
    解构语法，”_“ 可省略
     */
    @Test
    fun test() {
        val list = listOf("1", "2", "3")
        val (val1, _, val3) = list
        println("val1 = $val1 val3 = $val3")
    }
}