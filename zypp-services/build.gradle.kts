plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.kotlin.compose)

//	Firebase
	alias(libs.plugins.google.gms.google.services)
}

android {
	namespace = "com.example.xyzen"
	compileSdk = 35

	defaultConfig {
		applicationId = "com.example.xyzen"
		minSdk = 27
		targetSdk = 35
		versionCode = 1
		versionName = "1.0"

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}

	buildTypes {
		release {
			isMinifyEnabled = false
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro"
			)
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}
	kotlinOptions {
		jvmTarget = "11"
	}
	buildFeatures {
		compose = true
	}
}

dependencies {

	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.lifecycle.runtime.ktx)
	implementation(libs.androidx.activity.compose)
	implementation(platform(libs.androidx.compose.bom))
	implementation(libs.androidx.ui)
	implementation(libs.androidx.ui.graphics)
	implementation(libs.androidx.ui.tooling.preview)
	implementation(libs.androidx.material3)
	testImplementation(libs.junit)
	androidTestImplementation(libs.androidx.junit)
	androidTestImplementation(libs.androidx.espresso.core)
	androidTestImplementation(platform(libs.androidx.compose.bom))
	androidTestImplementation(libs.androidx.ui.test.junit4)
	debugImplementation(libs.androidx.ui.tooling)
	debugImplementation(libs.androidx.ui.test.manifest)

//	Navigation Dependencies
	implementation(libs.androidx.navigation.compose)

//	Icons Dependencies
	implementation(libs.androidx.material.icons.extended)

//	Firebase SDK Dependencies
	implementation(libs.firebase.auth)
	implementation(libs.androidx.credentials)
	implementation(libs.androidx.credentials.play.services.auth)
	implementation(libs.googleid)
	implementation(libs.firebase.database)
	implementation(libs.firebase.firestore)
	implementation(libs.firebase.storage)

//	Coroutines Dependencies
	implementation(libs.kotlinx.coroutines.core)
	implementation(libs.kotlinx.coroutines.android)

//	Retrofit Dependencies
	implementation(libs.retrofit)
	implementation(libs.converter.gson)

//	Ktor Dependencies
	implementation(libs.ktor.client.core)

//	ExoPlayer Dependencies for Video Playback
	implementation(libs.androidx.media3.exoplayer)
	implementation(libs.androidx.media3.exoplayer.dash)
	implementation(libs.androidx.media3.ui)

//	Coil for Image Loading
	implementation(libs.coil.compose)
	implementation(libs.coil.network.okhttp)

//	Compose Pager for snapping behavior
	implementation(libs.androidx.foundation)

//	Accompaist Pager for snapping behavior
	implementation("com.google.accompanist:accompanist-pager:0.30.1")
	implementation("com.google.accompanist:accompanist-pager-indicators:0.30.1")
}