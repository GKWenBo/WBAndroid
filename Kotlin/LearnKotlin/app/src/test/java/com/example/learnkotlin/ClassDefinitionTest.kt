package com.example.learnkotlin

import org.junit.Test
import java.util.Locale
import java.util.Locale.getDefault

class People(
    _name: String,
    _age: Int,
    _isNormal: Boolean
) {
    var name = _name
        get() = field.capitalize()
        set(value) {
            field = value.trim()
        }
    var age = _age
    var isNormal = _isNormal

    constructor(name: String): this(name, 10, true)

    constructor(name: String, age: Int = 30): this(name, age, false) {
        this.name = name.uppercase(getDefault())
    }

    init {
        require(age > 0, { "年龄必须大于0" })
        require(name.isNotBlank(), { "姓名不能为空" })
    }
}

class Student(
    _name: String,
    _age: Int
) {
    var name = _name
    var age = _age

    private  var hobby = "music"
    var score = 10
    var subject: String

    /// 延迟初始化
    lateinit var equipment: String

    /// 懒加载
    val confg by lazy {
        loadConfig()
    }

    private fun loadConfig(): String {
        println("loading")
        return "xxxx"
    }
    init {
        println("initializing student...")
        subject = "math"
    }

    /// 便利初始化
    constructor(_name: String): this(_name, 10) {
        score = 20
    }

    fun ready() {
        equipment = "sharp knife"
    }

    fun battle() {
        if (::equipment.isInitialized) println(equipment)
    }


}

class ClassDefinitionTest {

    @Test
    fun main() {
        val p = People("Jack", 18, true)

        val p1 = People("Rose")

        val p2 = People("ZhangSan", 20)
        println(p)
        println(p1)
        println(p2)


        val student = Student("Jack")
        student.ready()
        student.battle()
        student.confg
    }
}