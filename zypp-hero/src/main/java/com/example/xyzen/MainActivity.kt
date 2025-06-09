package com.example.xyzen

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.xyzen.screens.MainScreen

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContent {
			MainScreen()
		}
	}

	fun signOutFromApp() {
		startActivity(
			Intent(this, AuthenticationActivity::class.java).also {
				finish()
			}
		)
	}
}