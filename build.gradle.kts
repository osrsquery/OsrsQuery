
plugins {
    application
    id("org.jetbrains.kotlin.jvm") version "1.5.10"
}

group = "com.query"
version = "1.0"

repositories {
    mavenCentral()
}
dependencies {
    implementation(kotlin("stdlib"))
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(11))

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
       kotlinOptions.jvmTarget = "11"
}

