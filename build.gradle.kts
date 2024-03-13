import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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
    implementation("io.netty:netty-all:4.1.70.Final")
    implementation("com.xenomachina:kotlin-argparser:2.0.7")
    implementation("org.jetbrains.kotlinx:kotlinx-cli-jvm:0.3.4")
    implementation("com.google.guava:guava:31.0.1-jre")
    implementation("com.squareup.okhttp:okhttp:2.7.5")
    implementation("commons-lang:commons-lang:2.6")
    implementation("org.jsoup:jsoup:1.14.3")
    implementation("org.xbib.elasticsearch:joptsimple:6.3.2.1")
    implementation("io.guthix:js5-filestore:0.5.0")
    implementation("io.guthix:js5-container:0.5.0")
    // https://mvnrepository.com/artifact/commons-io/commons-io
    implementation("commons-io:commons-io:2.11.0")


}


val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}