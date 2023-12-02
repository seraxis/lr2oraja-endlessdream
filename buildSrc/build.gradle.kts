plugins {
    id("org.gradle.kotlin.kotlin-dsl") version "4.2.1"
}


repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.kotlin.serialization.gradle.plugin)
}
