package com.wb.example.demo_02

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

/**
 * 协程基础（对照 Swift async/await）。
 * Week1 检查点：并发拉两个接口并合并结果。
 */
object CoroutineExercises {

    // suspend ≈ Swift 的 async；delay ≈ Task.sleep（不阻塞线程）
    private suspend fun fetchUser(): String {
        delay(500)
        return "User(id=1, name=wenbo)"
    }

    private suspend fun fetchPosts(): List<String> {
        delay(800)
        return listOf("Post#1", "Post#2", "Post#3")
    }

    // coroutineScope + async/await ≈ Swift 的 async let / TaskGroup 并发
    suspend fun runConcurrencyDemo(): String = coroutineScope {
        val userDeferred = async { fetchUser() }   // 立即启动，不阻塞
        val postsDeferred = async { fetchPosts() } // 与上面并发执行
        val user = userDeferred.await()            // ≈ try await
        val posts = postsDeferred.await()
        "合并 -> $user | ${posts.size} 篇帖子"
    }
}
