plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
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

    // for google maps
    buildFeatures {
        buildConfig = true
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Firebase BoM to manage Firebase library versions
    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))
    // Add Firebase Authentication without a version (managed by the BoM)
    implementation("com.google.firebase:firebase-auth")
    // Add Firestore dependency
    implementation("com.google.firebase:firebase-firestore:25.1.1")

    implementation(libs.espresso.intents)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    //espresso intents
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.6.1")

    // for google map
    implementation("com.google.android.gms:play-services-maps:latest_version")

}
