package com.example.xyzen.model

import com.example.xyzen.model.Video

data class User(
	val id: String = "",
	val username: String = "",
	val email: String = "",
	val profileImage: String? = null,
	val bio: String = "",
	val videos: List<String> = emptyList() // List of video IDs
)