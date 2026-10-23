plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
 namespace = "ir.mehrineh.app"
 compileSdk = 35
 defaultConfig { applicationId = "ir.mehrineh.app"; minSdk = 26; targetSdk = 35; versionCode = 4; versionName = "4.0" }
 buildTypes { release { isMinifyEnabled = false } }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget = "17" }
}
