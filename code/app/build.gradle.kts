plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.projectapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.projectapp"
        minSdk = 24
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
}

dependencies {
    implementation(libs.firebase.storage) // Firebase Storage for image upload
    implementation(libs.firebase.auth)   // Firebase Authentication (optional, for user authentication)
    implementation(libs.imagepicker)      // Use only this one
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.gridlayout)
    implementation(libs.espresso.intents)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
