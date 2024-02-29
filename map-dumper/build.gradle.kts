import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "com.query"
version = "1.0"

dependencies {
    implementation(project(":core"))
    implementation("io.insert-koin:koin-core:${findProperty("koinVersion")}")
    implementation("io.insert-koin:koin-logger-slf4j:${findProperty("koinLogVersion")}")
    implementation("ch.qos.logback:logback-classic:${findProperty("logbackVersion")}")
    implementation("com.michael-bull.kotlin-inline-logger:kotlin-inline-logger-jvm:${findProperty("inlineLoggingVersion")}")
    implementation("it.unimi.dsi:fastutil:${findProperty("fastUtilVersion")}")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${findProperty("jacksonVersion")}")
}
