package com.example.chatclient

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform