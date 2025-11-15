import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.serialization") version "2.2.20"
    id("org.liquibase.gradle") version "2.2.1"
}

group = "com.thoni"
version = "0.1.0"

repositories {
    mavenCentral()
}

val ktorVersion = "2.3.11"
val exposedVersion = "0.53.0"
val liquibaseVersion = "4.28.0"
val sqliteJdbcVersion = "3.45.2.0"
val defaultJdbcUrl = System.getenv("SQLITE_JDBC_URL") ?: "jdbc:sqlite:data/mvp.sqlite"

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.xerial:sqlite-jdbc:$sqliteJdbcVersion")

    implementation("org.liquibase:liquibase-core:$liquibaseVersion")
    liquibaseRuntime("org.liquibase:liquibase-core:$liquibaseVersion")
    liquibaseRuntime("org.xerial:sqlite-jdbc:$sqliteJdbcVersion")
    liquibaseRuntime("org.slf4j:slf4j-simple:2.0.13")

    implementation("ch.qos.logback:logback-classic:1.5.6")

    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktorVersion")
}

application {
    mainClass.set("com.thoni.ApplicationKt")
}

liquibase {
    activities.register("main") {
        this.arguments = mapOf(
            "changeLogFile" to "liquibase/changelog-root.yaml",
            "url" to defaultJdbcUrl,
            "driver" to "org.sqlite.JDBC"
        )
    }
    runList = "main"
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
