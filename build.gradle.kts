buildscript {
    repositories {
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
        jcenter()
    }
    dependencies {
        classpath("com.apollographql.apollo:apollo-gradle-plugin:1.2.2")
    }
}

apply(plugin = "com.apollographql.android")

plugins {

    val kotlinVersion = "1.3.60"

    application
    kotlin("jvm") version kotlinVersion
    java // Required by at least JUnit.

    // Plugin which checks for dependency updates with help/dependencyUpdates task.
    id("com.github.ben-manes.versions") version "0.27.0"

    // Plugin which can update Gradle dependencies, use help/useLatestVersions
    id("se.patrikerdes.use-latest-versions") version "0.2.13"

}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("com.apollographql.apollo:apollo-runtime:1.2.2")

    implementation("org.jetbrains:annotations:13.0")
    testImplementation("org.jetbrains:annotations:13.0")
}

repositories {
    mavenCentral()
    mavenLocal()
    jcenter()
}

tasks.withType<com.apollographql.apollo.gradle.ApolloCodegenTask> {
    generateKotlinModels.set(true)
}