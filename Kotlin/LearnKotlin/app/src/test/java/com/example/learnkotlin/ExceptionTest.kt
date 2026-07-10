package com.example.learnkotlin

import org.junit.Test

class ExceptionTest {

    @Test
    fun main() {
        var number: Int? = null
        try {
            checkOperation(number)

        } catch (e: Exception) {
            println(e)
        }
    }

    fun checkOperation(number: Int?) {
        number ?: throw UnSkilledException()
    }

    class UnSkilledException(): IllegalArgumentException("操作不当！")
}