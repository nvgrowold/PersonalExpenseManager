plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.personalexpensemanager"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.personalexpensemanager"
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
}
//to find out which exact classes or methods are deprecated
tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Xlint:deprecation")
}

dependencies {
    implementation("com.google.firebase:firebase-auth:22.3.0")
    implementation("com.google.firebase:firebase-firestore:24.10.2")
    implementation ("com.google.firebase:firebase-storage:20.3.0")  //store PDF IR form
    implementation("com.google.android.gms:play-services-auth:20.7.0") // ✅ Required for Google Sign-In

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    //RoomDB
    implementation(libs.room.common)
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)

    //retrofit2
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    //Glide load image/gif
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

    //BCrypt: password hashing
    implementation("org.mindrot:jbcrypt:0.4")

    //modern imageView, round shape
    implementation ("com.google.android.material:material:1.12.0")
    
    //export ir form to pdf using iTEXT
    implementation ("com.itextpdf:itext7-core:7.1.15")




}

// Apply the Google Services plugin at the BOTTOM of the file
apply(plugin = "com.google.gms.google-services")

