package com.example.learnkotlin

import org.junit.Test

class MapTest {

    @Test
    fun main() {
        val map = mapOf("1" to 1, "2" to 2, "3" to 3)

        val map1 = mapOf(Pair("1", 1))

        println(map["1"])
        println(map.getOrElse("1"){ 1 })
        println(map.getOrDefault("1", 0))
    }

}