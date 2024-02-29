repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://maven.google.com/")

}

plugins {
    kotlin("jvm") version "1.8.21"
}


allprojects {
    apply(plugin = "kotlin")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    group = "com.query"
    version = "1.0.0"

    repositories {
        mavenCentral()
        mavenLocal()
    }

    dependencies {
        implementation(kotlin("stdlib-jdk8"))

        implementation("com.displee:rs-cache-library:${findProperty("displeeCacheVersion")}")
        implementation("io.github.microutils:kotlin-logging:1.12.5")
        implementation("org.slf4j:slf4j-simple:1.7.29")
        implementation("me.tongfei:progressbar:0.9.2")
        implementation("com.google.code.gson:gson:2.8.9")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    }

}


