// Apply Shadow plugin to create a fat JAR with dependencies
apply(plugin = "com.github.johnrengelman.shadow")

dependencies {
    implementation(project(":tools"))
    implementation(project(":cache"))

    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.formdev:flatlaf:3.2.5")
    implementation("com.formdev:flatlaf-extras:3.2.5")
    implementation("com.formdev:flatlaf-intellij-themes:3.2.5")
    // https://mvnrepository.com/artifact/org.apache.xmlgraphics/batik-transcoder
    implementation("org.apache.xmlgraphics:batik-transcoder:1.17")


}

tasks {
    shadowJar {
        archiveBaseName.set(archiveBaseName.get())
        archiveClassifier.set("")
    }
}