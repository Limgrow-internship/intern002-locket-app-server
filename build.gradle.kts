plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
}

group = "com.example"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

dependencies {
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.config.yaml)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)

    implementation("org.jetbrains.exposed:exposed-core:0.41.1") // (Check version mới nhất)

    implementation("org.jetbrains.exposed:exposed-jdbc:0.41.1")

    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:0.41.1")

    implementation("org.postgresql:postgresql:42.5.0")

    implementation("com.zaxxer:HikariCP:5.0.1")
}

ktlint {
    version.set("1.2.1") // Version của cái TOOL Ktlint
}
