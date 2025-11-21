plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
}

group = "com.intern002.locketapp"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"

    // ✅ Cho phép chạy bằng: ./gradlew run
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=true")
}

repositories {
    mavenCentral()
}

dependencies {
    // --- Ktor core ---
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.logback.classic)
    implementation("io.ktor:ktor-server-status-pages:2.3.12") // Thêm dòng này

    // --- Exposed ORM ---
    implementation("org.jetbrains.exposed:exposed-core:0.41.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.41.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.41.1")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:0.41.1")

    // --- Database driver + connection pool ---
    implementation("org.postgresql:postgresql:42.7.3") // ⚡ update version mới hơn
    implementation("com.zaxxer:HikariCP:5.1.0")

    // --- Dependency Injection (Koin) ---
    implementation("io.insert-koin:koin-ktor:3.5.3")
    implementation("io.insert-koin:koin-logger-slf4j:3.5.3")

    // --- JWT + Security ---
    implementation("com.auth0:java-jwt:4.4.0")
    implementation("at.favre.lib:bcrypt:0.10.2")

    // --- Utils ---
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0") // bản ổn định mới hơn

    // --- Environment Variables (.env) ---
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")

    // --- Firebase ---
    implementation("com.google.firebase:firebase-admin:9.2.0")

    // --- Test ---
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
}

ktlint {
    version.set("1.2.1")
    ignoreFailures.set(true)
}
