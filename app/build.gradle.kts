plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.gamecatalog"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.gamecatalog"
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
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
    // https://mvnrepository.com/artifact/com.squareup.retrofit2/retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    // https://mvnrepository.com/artifact/com.squareup.retrofit2/converter-gson

    // OkHttp — HTTP-запросы
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // Room — база данных
    implementation ("androidx.room:room-runtime:2.8.3")
    annotationProcessor ("androidx.room:room-compiler:2.8.3")

    // ViewModel + LiveData
    implementation ("androidx.lifecycle:lifecycle-livedata:2.8.2")
    implementation ("androidx.lifecycle:lifecycle-viewmodel:2.8.2")

    // RecyclerView
    implementation ("androidx.recyclerview:recyclerview:1.3.2")

    // Material Components (UI)
    implementation ("com.google.android.material:material:1.12.0")

    // Glide — загрузка изображений
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")

}