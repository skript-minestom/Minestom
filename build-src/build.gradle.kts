plugins {
    `kotlin-dsl`
}

val javaVersion = System.getenv("JAVA_VERSION") ?: "25"

java {
    toolchain.languageVersion = JavaLanguageVersion.of(javaVersion)
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}
