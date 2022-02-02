
plugins {
    application
    id("org.jetbrains.kotlin.jvm") version "1.5.10"
}

group = "com.query"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://maven.google.com/")
}
dependencies {
    implementation(kotlin("stdlib"))

    implementation("io.github.microutils:kotlin-logging:1.12.5")
    implementation("org.slf4j:slf4j-simple:1.7.29")
    implementation("me.tongfei:progressbar:0.9.2")
    implementation("com.beust:klaxon:5.5")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("net.lingala.zip4j:zip4j:2.9.1")
    implementation("com.displee:rs-cache-library:6.8")
    implementation("io.netty:netty-all:5.0.0.Alpha2")



}


