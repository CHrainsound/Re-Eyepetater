plugins {
    alias(libs.plugins.android.library)
}

// 网络凭据统一由根目录 secrets.properties（已被 .gitignore，禁止提交）注入，
// 生成 com.example.core.network.BuildConfig 字段，源码中不再保留任何明文密钥。
val secrets = java.util.Properties().apply {
    val f = rootProject.file("secrets.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
fun secret(key: String): String = secrets.getProperty(key)?.trim().orEmpty()

android {
    namespace = "com.example.core.network"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "X_API_KEY", "\"${secret("X_API_KEY")}\"")
        buildConfigField("String", "X_THEFAIR_APPID", "\"${secret("X_THEFAIR_APPID")}\"")
        buildConfigField("String", "X_THEFAIR_AUTH", "\"${secret("X_THEFAIR_AUTH")}\"")
        buildConfigField("String", "X_THEFAIR_CID", "\"${secret("X_THEFAIR_CID")}\"")
        buildConfigField("String", "CLIENT_UA", "\"${secret("CLIENT_UA")}\"")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core:core_model"))
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging)
    testImplementation(libs.junit)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
