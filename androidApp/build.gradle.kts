plugins {
    alias(libs.plugins.convention.android.application)
}

android {
    namespace = "com.autolink.android"
    defaultConfig {
        applicationId = "com.autolink"
        versionCode = 1
        versionName = "0.1.0-poc"
    }
}

dependencies {
    implementation(projects.shared)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.androidx.activity.compose)
}
