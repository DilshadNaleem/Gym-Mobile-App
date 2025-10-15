plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.kolonnawabarbellgym"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.kolonnawabarbellgym"
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

    // ✅ FIX FOR DUPLICATE META-INF FILES (like NOTICE.md)
    packaging {
        resources {
            excludes += "/META-INF/{LICENSE*,NOTICE*,README*}"
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // ✅ JavaMail dependencies for sending email directly from Android
    implementation("com.sun.mail:android-mail:1.6.7")
    implementation("com.sun.mail:android-activation:1.6.7")
}
