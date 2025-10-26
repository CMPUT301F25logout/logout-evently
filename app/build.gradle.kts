plugins {
    alias(libs.plugins.android.application)
    id("com.diffplug.spotless") version "8.0.0"
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.evently"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.evently"
        minSdk = 30
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

spotless {
    java {
        // Need to explicitly specify target for android projects.
        target("src/*/java/**/*.java")

        importOrder("java|javax", "android|androidx", "")

        removeUnusedImports()
        forbidWildcardImports()

        // Apply a specific flavor of google-java-format
        palantirJavaFormat("2.81.0").style("AOSP").formatJavadoc(false)
        // Fix formatting of type annotations
        formatAnnotations()

        // QoL stuff
        trimTrailingWhitespace()
        endWithNewline()
    }

    format("xml") {
        target("src/*/res/**/*.xml")
        trimTrailingWhitespace()
        endWithNewline()
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
    implementation(platform("com.google.firebase:firebase-bom:34.3.0"))
    implementation("com.google.firebase:firebase-firestore")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.0.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.0.1")
}