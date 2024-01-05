buildscript {
    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.8.21"))
    }
}

plugins {
    kotlin("jvm") version "1.8.21"
}

allprojects {
    apply(plugin = "kotlin")
    apply(plugin = "idea")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    group = "com.query"
    version = "1.0.0"

    java.sourceCompatibility = JavaVersion.VERSION_19

    repositories {
        mavenCentral()
        mavenLocal()
    }

    dependencies {
        implementation(kotlin("stdlib-jdk8"))
        testImplementation("org.junit.jupiter:junit-jupiter-api:${findProperty("junitVersion")}")
        implementation("com.beust:klaxon:5.5")
    }

    tasks {
        compileKotlin {
            kotlinOptions.jvmTarget = JavaVersion.VERSION_19.toString()
            // https://youtrack.jetbrains.com/issue/KT-4779/Generate-default-methods-for-implementations-in-interfaces
            kotlinOptions.freeCompilerArgs = listOf("-Xinline-classes", "-Xcontext-receivers", "-Xjvm-default=all-compatibility")
        }
        compileTestKotlin {
            kotlinOptions.jvmTarget = JavaVersion.VERSION_19.toString()
            kotlinOptions.freeCompilerArgs = listOf("-Xinline-classes", "-Xcontext-receivers", "-Xjvm-default=all-compatibility")
        }
    }

}
