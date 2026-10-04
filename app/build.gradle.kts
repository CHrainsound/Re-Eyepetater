plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

// 签名材料从 keystore.properties（已被 .gitignore，禁止提交）读取；
// 未提供该文件时跳过签名配置，普通构建不受影响。模板见 keystore.properties.example。
val keystoreProps = java.util.Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val hasSigning = keystoreProps.getProperty("storeFile")?.isNotBlank() == true

android {
    namespace = "com.example.eyepetater"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        // 审查修复 P1-3：com.example.* 包名会被 Google Play 拒绝，改为自有标识；
        // namespace 暂保持不变（包目录重命名作为独立迁移任务，见审查修复记录）。
        applicationId = "com.scdomm.eyepetater"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (hasSigning) {
            create("release") {
                storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            if (hasSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
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
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":ept_category"))
    implementation(project(":ept_dicover"))
    implementation(project(":ept_home"))
    implementation(project(":ept_daily"))
    implementation(project(":ept_notify"))
    implementation(project(":core:core_model"))
    implementation(project(":core:core_network"))
    implementation(project(":core:core_common"))
    implementation(project(":ept_person"))
    implementation(project(":ept_hot"))
    implementation(libs.androidx.appcompat)
    implementation(libs.therouter.api)
    ksp(libs.therouter.compiler)
    implementation(libs.material)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.gsy.video.player)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.fragment.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
ksp {
    arg("therouter.moduleName", ":app")
}
