package com.littleb01s.ashasakhichat.presentation.screens

fun extractYouTubeVideoId(url: String): String? {
    val regex = Regex("(?:v=|youtu.be/|embed/|v/|shorts/)([A-Za-z0-9_-]{11})")
    val match = regex.find(url)
    return match?.groups?.get(1)?.value
} 