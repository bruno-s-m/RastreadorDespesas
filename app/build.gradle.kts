    plugins {
        alias(libs.plugins.android.application)
    }

    android {
        namespace = "com.example.rastreadordespesas"
        compileSdk {
            version = release(36)
        }

        defaultConfig {
            applicationId = "com.example.rastreadordespesas"
            minSdk = 26
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
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
    }

    dependencies {
        // Bibliotecas Padrão do Google
        implementation("com.google.android.material:material:1.12.0")
        implementation(libs.appcompat)
        implementation(libs.material)
        implementation(libs.activity)
        implementation(libs.constraintlayout)
        implementation(libs.navigation.fragment)
        implementation(libs.navigation.ui)

        // Testes
        testImplementation(libs.junit)
        androidTestImplementation(libs.ext.junit)
        androidTestImplementation(libs.espresso.core)

        // --- AQUI ESTÃO AS SUAS ADIÇÕES (CORRIGIDAS) ---

        // Room (Banco de Dados)
        val roomVersion = "2.6.1"
        implementation("androidx.room:room-runtime:$roomVersion")
        annotationProcessor("androidx.room:room-compiler:$roomVersion") // Para Java usamos annotationProcessor

        // Lifecycle (Para evitar crash em threads)
        implementation("androidx.lifecycle:lifecycle-common-java8:2.6.2")

        // MPAndroidChart (Gráficos)
        implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    }