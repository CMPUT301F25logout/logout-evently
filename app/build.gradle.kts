import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    id("com.diffplug.spotless")
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

        // Load API credentials
        val keystoreFile = project.rootProject.file("keys.properties")
        val properties = Properties()
        properties.load(keystoreFile.inputStream())

        val gclientID = properties.getProperty("GOOGLE_CLIENT_ID") ?: ""

        buildConfigField(
            type = "String",
            name = "GOOGLE_CLIENT_ID",
            value = gclientID
        )
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
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
    lint {
        checkAllWarnings = true
        abortOnError = true
        warningsAsErrors = false
    }
}

spotless {
    java {
        // Need to explicitly specify target for android projects.
        target("src/*/java/**/*.java")

        removeUnusedImports()
        forbidWildcardImports()

        // Apply a specific flavor of google-java-format
        palantirJavaFormat("2.81.0").style("AOSP").formatJavadoc(false)

        // Fix formatting of type annotations
        formatAnnotations()

        // Fix import order
        importOrder("java|javax", "android|androidx", "", "com.example.evently")

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

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.play.services.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.recyclerview)

    testImplementation(libs.junit)
    androidTestImplementation(libs.runner)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}