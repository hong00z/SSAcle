import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    id("androidx.navigation.safeargs.kotlin")
    id("kotlin-parcelize")
    id("com.google.gms.google-services") // FCM
}

android {
    namespace = "com.example.firstproject"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.firstproject"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "NATIVE_APP_KEY", "${properties["NATIVE_APP_KEY"]}")
        manifestPlaceholders["NATIVE_APP_KEY"] = "${properties["NATIVE_APP_KEY"]}"
    }

    val localProperties = Properties().apply {
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            load(localPropertiesFile.inputStream())
        }
    }
    val openaiApiKey: String = localProperties.getProperty("OPENAI_API_KEY", "")

    buildTypes {
        release {
            //openaiAPI key 가져오려고 씁니다.
            buildConfigField("String", "OPENAI_API_KEY", "\"$openaiApiKey\"")

            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            buildConfigField("String", "OPENAI_API_KEY", "\"$openaiApiKey\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true  // BuildConfig 활성화

    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}

dependencies {

    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.material3.android)
    implementation(libs.litert.gpu)
    implementation(libs.litert)
    implementation(libs.transport.api)

    val nav_version = "2.8.6"
    // Navigation
    // Jetpack Compose integration
    implementation("androidx.navigation:navigation-compose:$nav_version")
    // Views/Fragments integration
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")
    // Feature module support for Fragments
    implementation("androidx.navigation:navigation-dynamic-features-fragment:$nav_version")

    implementation("com.google.android.material:material:1.9.0")

    // RequestResult 라이브러리
    implementation("com.github.rootachieve:RequestResult:0.1.0")

    // Coil
    implementation ("io.coil-kt:coil-compose:2.4.0")

    implementation(platform("androidx.compose:compose-bom:2025.01.01"))
    // 기본 Compose 라이브러리
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.foundation:foundation-layout")
    implementation("androidx.compose.material:material")

    // XML에서 ComposeView를 사용하기 위한 추가 의존성
    implementation("androidx.activity:activity-compose")
    implementation("androidx.compose.ui:ui-viewbinding")

    // Lifecycle 관련 (XML과 Compose 상태 동기화)
    implementation("androidx.lifecycle:lifecycle-runtime-compose")
    implementation("androidx.compose.runtime:runtime")

    // 개발 편의성 및 디버깅 (선택 사항)
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // pdf 변환을 위한 의존성
    implementation("com.itextpdf:itext7-core:7.2.5") // 최신 버전 확인 후 변경 가능
    implementation("com.tom-roush:pdfbox-android:2.0.24.0")
    implementation("com.itextpdf:html2pdf:6.0.0")

    // 웹과 연동 및 코루틴 관련 의존성
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")


    // Socket.io
    implementation("io.socket:socket.io-client:2.1.1")

    // 카카오 로그인
    implementation("com.kakao.sdk:v2-user:2.12.0")
    implementation("com.jakewharton.timber:timber:5.0.1")

    // 프로필 이미지 불러오기 (Glide 사용)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // Retrofit http 통신
    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))

    // define any required OkHttp artifacts without version
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // 인디케이터 기능
    implementation("com.tbuonomo:dotsindicator:4.3")


    // ai 관련 (카메라)

    implementation("androidx.camera:camera-core:1.1.0")
    implementation("androidx.camera:camera-lifecycle:1.1.0")
    implementation("androidx.camera:camera-view:1.1.0")
    implementation("androidx.camera:camera-camera2:1.3.0")

    // ai 관련 tensor 계산 용
    implementation("org.tensorflow:tensorflow-lite-select-tf-ops:2.14.0")

    // 도넛 차트 그리는것
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // FCM
    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-analytics")

    // WebRTC
//    implementation("org.webrtc:google-webrtc:1.0.+")
    implementation("io.github.haiyangwu:mediasoup-client:3.4.0")

    // 프로필 원형으로 받아 오는 패키지
    implementation("de.hdodenhof:circleimageview:3.1.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
}

