plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.ept.search"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.flexbox)
    implementation(libs.material)
    testImplementation(libs.junit)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging)
    implementation(libs.glide.okhttp)
    implementation(project(":core:core_model"))
    implementation(project(":core:core_network"))
    implementation(project(":core:core_common"))
    implementation(project(":core:core_media"))
    implementation(project(":ept_person"))
    implementation(project(":ept_hot"))
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.glide)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    implementation(libs.therouter.api)
    ksp(libs.therouter.compiler)
}
ksp {
    arg("therouter.moduleName", ":ept_search")
}