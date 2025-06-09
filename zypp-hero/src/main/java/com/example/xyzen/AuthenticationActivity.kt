package com.example.xyzen

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.xyzen.firebase.FirebaseServiceClass
import com.example.xyzen.screens.AuthenticationScreen

class AuthenticationActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		if (FirebaseServiceClass().isUserLoggedIn()) {
			startActivity(Intent(this, MainActivity::class.java)).also {
				finish()
			}
			return
		}

		setContent {
			AuthenticationScreen()
		}
	}
}