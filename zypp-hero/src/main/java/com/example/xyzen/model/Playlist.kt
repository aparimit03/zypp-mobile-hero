package com.example.xyzen.model

import com.google.firebase.Timestamp

data class Playlist(
	val id: String = "",
	val userId: String = "",
	val name: String = "",
	val description: String = "",
	val coverImageUrl: String? = null,
	val videos: List<String> = emptyList(),
	val timestamp: Timestamp = Timestamp.now(),
	val isPublic: Boolean = true
)