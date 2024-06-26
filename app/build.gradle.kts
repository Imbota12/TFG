plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "com.example.tfg_sistematienda"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.tfg_sistematienda"
        minSdk = 33
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("com.google.android.material:material:1.11.0")
    implementation("org.postgresql:postgresql:42.2.9")
    implementation("org.mindrot:jbcrypt:0.4")
    implementation("com.google.zxing:core:3.4.1")
    implementation("com.journeyapps:zxing-android-embedded:4.2.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.github.DantSu:ESCPOS-ThermalPrinter-Android:3.3.0")
    implementation(libs.play.services.maps)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation ("org.apache.poi:poi:5.2.3")
    implementation ("org.apache.poi:poi-ooxml:5.2.3")
}



