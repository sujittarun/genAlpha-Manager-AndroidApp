plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
}

android {
  namespace = "com.genalpha.cricketacademy"
  compileSdk = 35

  defaultConfig {
    applicationId = "com.genalpha.cricketacademy"
    minSdk = 24
    targetSdk = 35
    versionCode = 68
    versionName = "1.0.68"

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

  kotlinOptions {
    jvmTarget = "17"
  }

  buildFeatures {
    compose = true
  }

  composeOptions {
    kotlinCompilerExtensionVersion = "1.5.14"
  }

  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }

  applicationVariants.all {
    outputs.all {
      val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
      output.outputFileName = "genAlpha-manager.apk"
    }
  }
}

dependencies {
  implementation("androidx.core:core-ktx:1.13.1")
  implementation("androidx.appcompat:appcompat:1.7.0")
  implementation("com.google.android.material:material:1.12.0")
  implementation("androidx.activity:activity-compose:1.9.1")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
  implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")
  implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")
  implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
  implementation(platform("androidx.compose:compose-bom:2024.06.00"))
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.ui:ui-tooling-preview")
  implementation("androidx.compose.foundation:foundation")
  implementation("androidx.compose.material3:material3")
  implementation("androidx.compose.material:material-icons-extended")
  implementation("com.squareup.okhttp3:okhttp:4.12.0")
  implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
  implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
  implementation("com.google.zxing:core:3.5.3")
  debugImplementation("androidx.compose.ui:ui-tooling")
  debugImplementation("androidx.compose.ui:ui-test-manifest")
  testImplementation("junit:junit:4.13.2")
  testImplementation("org.json:json:20240303")
}

// ---------------------------------------------------------------------
// pay.html runs in the WebView from file:///android_asset/ and loads
// ./supabase-config.js while parsing, capturing it into a const. That
// file did not exist, so window.GEN_ALPHA_SUPABASE_CONFIG was undefined,
// supabaseConfig.url was undefined, and every payment signal the page
// posts — payment attempted, proof nudge, manager alert — was skipped by
// the page's own `!supabaseConfig.url` guard. The UPI intent still fired,
// so it looked like it worked.
//
// Generated from SupabaseConfig.kt rather than committed, because a
// committed copy is what failed: assets/web/supabase-config.js sat there
// pointing at the decommissioned project long after the cutover.
// ---------------------------------------------------------------------
val generateWebConfig by tasks.registering {
    val src = file("src/main/java/com/genalpha/cricketacademy/data/SupabaseConfig.kt")
    val out = file("src/main/assets/supabase-config.js")
    inputs.file(src)
    outputs.file(out)
    doLast {
        val text = src.readText()
        val url = Regex("const val URL\\s*=\\s*\"([^\"]+)\"").find(text)?.groupValues?.get(1)
            ?: error("SupabaseConfig.kt: could not find URL")
        val key = Regex("const val ANON_KEY\\s*=\\s*\"([^\"]+)\"").find(text)?.groupValues?.get(1)
            ?: error("SupabaseConfig.kt: could not find ANON_KEY")
        out.parentFile.mkdirs()
        out.writeText(
            "// GENERATED from SupabaseConfig.kt at build time. Do not edit.\n" +
            "window.GEN_ALPHA_SUPABASE_CONFIG = {\n" +
            "  url: \"" + url + "\",\n" +
            "  anonKey: \"" + key + "\",\n" +
            "};\n"
        )
    }
}

tasks.named("preBuild") { dependsOn(generateWebConfig) }
