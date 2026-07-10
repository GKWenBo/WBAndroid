package com.example.learnkotlin

import org.junit.Test

class ClosureTest {

    @Test
    fun mian() {
        val getDiscountWords = configDiscountWords()
        println(getDiscountWords("洗发水"))
    }

    fun configDiscountWords(): (String) -> String {
        val currentYear = 2026
        val hour = (1..24).shuffled().last()
        return { goodsName: String ->
            "${currentYear}年，双11${goodsName}促销倒计时：$hour 小时"
        }
    }
}