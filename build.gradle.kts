import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.20"
}

group = "me.mariakhalusova"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://dl.bintray.com/kotlin/kotlin-datascience")
}

dependencies {
    testImplementation(kotlin("test-junit"))
    implementation("org.jetbrains.multik:multik-api:0.0.1-dev-7")
    implementation("org.jetbrains.multik:multik-default:0.0.1-dev-7")
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}