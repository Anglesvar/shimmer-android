import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    id("com.vanniktech.maven.publish")
}

android {
    namespace = "com.anglesvar.shimmer"
    compileSdk = 35

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    // Jetpack Compose
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Compose Foundation & Animation
    implementation("androidx.compose.foundation:foundation:1.7.6")
    implementation("androidx.compose.animation:animation:1.7.6")
    implementation("androidx.compose.animation:animation-core:1.7.6")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

// Load publishing properties
val properties = Properties().apply {
    val propsFile = project.file("gradle.properties")
    if (propsFile.exists()) {
        propsFile.inputStream().use { load(it) }
    }
}

val libraryVersion = properties.getProperty("LIBRARY_VERSION") ?: "1.0.0"
val libraryGroupId = properties.getProperty("GROUP_ID") ?: "com.anglesvar"
val libraryArtifactId = properties.getProperty("ARTIFACT_ID") ?: "shimmer"

// Configure Maven publishing with vanniktech plugin
mavenPublishing {
    coordinates(libraryGroupId, libraryArtifactId, libraryVersion)

    pom {
        name.set("Shimmer for Android")
        description.set("A modern, Kotlin-based shimmer effect library for Android with Jetpack Compose support")
        inceptionYear.set("2024")
        url.set("https://github.com/anglesvar/shimmer-android")

        licenses {
            license {
                name.set("BSD License")
                url.set("https://opensource.org/licenses/BSD-3-Clause")
                distribution.set("https://opensource.org/licenses/BSD-3-Clause")
            }
        }

        developers {
            developer {
                id.set("anglesvar")
                name.set("anglesvar")
                email.set("anglesvar@users.noreply.github.com")
                url.set("https://github.com/anglesvar")
            }
        }

        scm {
            connection.set("scm:git:git://github.com/anglesvar/shimmer-android.git")
            developerConnection.set("scm:git:ssh://github.com/anglesvar/shimmer-android.git")
            url.set("https://github.com/anglesvar/shimmer-android")
        }
    }

    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()
}
