package com.example.learnkotlin

import org.junit.Test

class SetTest {
    @Test
    fun main() {
        val set = setOf("1", "1", "2", "3")
        println(set.elementAt(0))
        println("count = ${set.count()}")

        val arr = set.toList()

        println(listOf("1", "1", "2", "3").distinct())

        val mutableSet = mutableSetOf("1")
        mutableSet += "jimmy"
        println(mutableSet)
    }
}