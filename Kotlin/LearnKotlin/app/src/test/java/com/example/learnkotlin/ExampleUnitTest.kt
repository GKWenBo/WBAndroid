package com.example.learnkotlin

import android.R
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    /*
    when使用
     */
    @Test
    fun testWhen() {
        val school = "小学"
        val level = when(school) {
            "学前班" -> "幼儿"
            "小学" -> "少儿"
            else -> {
                print("未知")
            }
        }
        println(level)
    }

    /*
    函数作为参数
     */
    @Test
    fun showOnBoard() {
        val getDiscountWords: (String, Int) -> String =  { goodsName: String, hour: Int ->
            var currentYear = 2027
            "${currentYear}年，双11${goodsName}促销倒计时：$hour 小时"
        }
        showOnBoard("卫生纸", getDiscountWords)
    }

    fun showOnBoard(goodsName: String, getDiscountWords: (String, Int) -> String) {
        val hour = (1..24).shuffled().last()
        println(getDiscountWords(goodsName, hour))
    }

}