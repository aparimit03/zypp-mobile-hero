package com.example.xyzen.model

import com.google.firebase.Timestamp

data class Video(
	val id: String = "",
	val userId: String = "",
	val caption: String = "",
	val videoUrl: String = "",
	val thumbnailUrl: String = "",
	val timestamp: Timestamp = Timestamp.now(),
	val likes: Int = 0,
	val views: Int = 0,
	val comments: List<Comment> = emptyList()
)