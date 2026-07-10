package com.example.learnkotlin

import org.junit.Test

class Player {
    var name = "Jack"
        get() = field.capitalize()
        set(value) {
            field = value.trim()
        }

    // 计算属性
    val rolledValue
        get() = (1..6).shuffled().first()
}

class ClassTest {

    @Test
    fun main() {
        val player = Player()
        player.name = "Rose "
        println(player.name)

        println(player.rolledValue)
    }
}