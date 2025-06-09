package com.example.xyzen.model

import com.google.firebase.Timestamp

data class Comment(
	val id: String = "",
	val userId: String = "",
	val text: String = "",
	val timestamp: Timestamp = Timestamp.now()
)
