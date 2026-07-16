package com.example.greatingkmp

import kotlin.random.Random

fun sayHello(to: String): String {
    val firstWord = if (Random.nextBoolean()) "Hi!" else "Hello!"
    return "$firstWord [$num] Guess what this is! > ${firstWord.reversed()}!"
}