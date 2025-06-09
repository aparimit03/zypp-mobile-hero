package com.example.xyzen.screens

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DisabledVisible
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xyzen.MainActivity
import com.example.xyzen.components.GradientButton
import com.example.xyzen.components.ModernTextField
import com.example.xyzen.firebase.FirebaseServiceClass
import com.example.xyzen.ui.theme.montserratFontFamily
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

@Preview(showBackground = true)
@Composable
fun AuthenticationScreen() {
	var isLogin by remember {
		mutableStateOf(true)
	}
	var email by remember {
		mutableStateOf("")
	}
	var password by remember {
		mutableStateOf("")
	}
	var userName by remember {
		mutableStateOf("")
	}
	var passwordVisible by remember {
		mutableStateOf(false)
	}
	var isLoading by remember {
		mutableStateOf(false)
	}
	var errorMessage by remember {
		mutableStateOf<String?>(null)
	}

	val coroutineScope = rememberCoroutineScope()
	val context = LocalContext.current

	val firebaseService = remember {
		FirebaseServiceClass()
	}

	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(16.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		Text(
			text = if (isLogin) "Login" else "Register",
			fontSize = 24.sp,
			color = Color.Black,
			fontFamily = montserratFontFamily,
			fontWeight = FontWeight.SemiBold,
			modifier = Modifier.padding(vertical = 16.dp)
		)
		Spacer(
			modifier = Modifier
				.height(20.dp)
		)
		if (!isLogin) {
			ModernTextField(
				value = userName,
				onValueChange = {
					userName = it
				},
				label = "Username",
				leadingIcon = Icons.Default.Person,
				modifier = Modifier.fillMaxWidth(),
				singleLine = true,
				keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
			)
			Spacer(modifier = Modifier.height(16.dp))
		}
		ModernTextField(
			value = email,
			onValueChange = {
				email = it
			},
			label = "Email",
			leadingIcon = Icons.Default.Email,
			modifier = Modifier.fillMaxWidth(),
			singleLine = true,
			keyboardOptions = KeyboardOptions(
				keyboardType = KeyboardType.Email,
				imeAction = ImeAction.Next
			)
		)

		Spacer(modifier = Modifier.height(16.dp))

		ModernTextField(
			value = password,
			onValueChange = {
				password = it
			},
			label = "Password",
			leadingIcon = Icons.Default.Lock,
			trailingIcon = {
				IconButton(onClick = { passwordVisible = !passwordVisible }) {
					Icon(
						imageVector = if (passwordVisible) Icons.Default.RemoveRedEye else Icons.Default.DisabledVisible,
						contentDescription = if (passwordVisible) "Hide password" else "Show password"
					)
				}
			},
			visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
			modifier = Modifier.fillMaxWidth(),
			singleLine = true,
			keyboardOptions = KeyboardOptions(
				keyboardType = KeyboardType.Password,
				imeAction = ImeAction.Done
			)
		)
		errorMessage?.let {
			Spacer(modifier = Modifier.height(8.dp))
			Text(
				text = it,
				color = MaterialTheme.colorScheme.error,
				style = MaterialTheme.typography.bodySmall
			)
		}

		Spacer(modifier = Modifier.height(24.dp))

		GradientButton(
			onClick = {
				if (!isLogin && userName.isBlank()) {
					errorMessage = "Username cannot be empty"
				} else if (email.isBlank()) {
					errorMessage = "Email cannot be empty"
				} else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
					errorMessage = "Please enter a valid email address"
				} else if (password.isBlank()) {
					errorMessage = "Password cannot be empty"
				} else if (!isLogin && password.length < 6) {
					errorMessage = "Password must be at least 6 characters"
				} else {
					isLoading = true
					errorMessage = null

					coroutineScope.launch {
						if (isLogin) {
							val result = firebaseService.signIn(email, password)
							handleAuthResult(result, context)
						} else {
							val result = firebaseService.register(email, password, userName)
							handleAuthResult(result, context)
						}
						isLoading = false
					}
				}
			},
			enabled = !isLoading,
			modifier = Modifier.fillMaxWidth(),
			text = if (isLogin) "Login" else "Register"
		)

		Spacer(modifier = Modifier.height(16.dp))

		TextButton(
			onClick = {
				isLogin = !isLogin
				errorMessage = null
			}
		) {
			Text(
				text = if (isLogin) "Don't have an account? Register" else "Already have an account? Login",
			)
		}
	}
}

private fun handleAuthResult(result: Result<FirebaseUser>, context: Context) {
	result.fold(
		onSuccess = {
			context.startActivity(
				Intent(context, MainActivity::class.java).also { intent ->
					intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
				}
			)
		},
		onFailure = { exception ->
			Log.d("Authentication", "Error: ${exception.message}")
			val errorMessage = when {
				exception.message?.contains("email address is already in use") == true ->
					"Email is already registered"

				exception.message?.contains("password is invalid") == true ->
					"Invalid password"

				exception.message?.contains("no user record") == true ->
					"No account found with this email"

				exception.message?.contains("auth credential is incorrect") == true ->
					"Some information is incorrect"

				else -> exception.message ?: "Authentication failed"
			}
			Toast.makeText(
				context,
				errorMessage,
				Toast.LENGTH_LONG
			).show()
		}
	)
}