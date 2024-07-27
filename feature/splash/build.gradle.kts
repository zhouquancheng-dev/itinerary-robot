plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hilt.android)
    kotlin("kapt")
    id("aleyn-router")
}

apply(rootProject.file("buildConfig.gradle.kts"))
val autoConfig: Map<String, Any> by extra

android {
    namespace = "com.example.splash"
    compileSdk = autoConfig["COMPILE_SDK"] as Int

    defaultConfig {
        minSdk = autoConfig["MIN_SDK"] as Int

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        resourceConfigurations.addAll(listOf("en", "zh-rCN"))
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
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    // Allow references to generated code
    kapt {
        correctErrorTypes = true
    }
}

composeCompiler {
    // 启用强跳过模式
    enableStrongSkippingMode = true
    // 启用内在记忆性能优化
    enableIntrinsicRemember = true
    // Compose 编译器将使用该目录转储编译器指标报告
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
    // 稳定性配置文件
    stabilityConfigurationFile = rootProject.layout.projectDirectory.file("compose_compiler_config.conf")
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:ui"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.android.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // hilt不能引入模块使用，在需要使用hilt的模块单独引入依赖
    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.android.compiler)
}