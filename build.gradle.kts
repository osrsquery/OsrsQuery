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
    }

}


