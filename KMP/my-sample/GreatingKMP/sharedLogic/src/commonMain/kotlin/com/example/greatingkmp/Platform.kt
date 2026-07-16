package com.example.greatingkmp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect val num: Int