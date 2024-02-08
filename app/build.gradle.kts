plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
    id("kotlin-kapt")
}

android {
    namespace = "leopardcat.studio.chitchat"
    compileSdk = 34

    defaultConfig {
        applicationId = "leopardcat.studio.chitchat"
        minSdk = 30
        targetSdk = 34
        versionCode = 11
        versionName = "1.0.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation ("androidx.navigation:navigation-compose:2.6.0")

    //landscapist (compose glide image)
    implementation ("com.github.skydoves:landscapist-glide:2.2.0")
    implementation ("com.github.skydoves:landscapist-coil:2.2.0")

    // Room
    implementation ("androidx.room:room-ktx:2.5.0")
    annotationProcessor("androidx.room:room-ktx:2.5.0")
    kapt ("androidx.room:room-compiler:2.5.0")

    //compose ViewModel
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")

    //compose LiveData
    implementation ("androidx.compose.runtime:runtime-livedata:1.5.1")

    /* Retrofit2 */
    implementation ("com.squareup.retrofit2:retrofit:2.3.0")
    implementation ("com.squareup.retrofit2:adapter-rxjava2:2.3.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.3.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:3.9.0")

    /* RxKotlin */
    implementation ("io.reactivex.rxjava2:rxandroid:2.0.1")
    implementation ("io.reactivex.rxjava2:rxkotlin:2.3.0")

    //lottie
    implementation ("com.airbnb.android:lottie-compose:6.1.0")

    //admob
    implementation ("com.google.android.gms:play-services-ads:22.4.0")

    //material icons
    implementation ("androidx.compose.material:material-icons-extended-android:1.5.4")

    //jsoup
    implementation ("org.jsoup:jsoup:1.15.3")

    //Glide
    implementation("com.github.bumptech.glide:glide:4.13.2")
    annotationProcessor("com.github.bumptech.glide:compiler:4.13.2")
    implementation("com.google.accompanist:accompanist-glide:0.10.0")

    //splashscreen api
    implementation ("androidx.core:core-splashscreen:1.0.0-alpha02")

    //inapp billing
    implementation ("com.android.billingclient:billing:6.0.1")
}