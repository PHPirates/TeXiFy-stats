plugins {

    val kotlinVersion = "2.1.0"

    application
    kotlin("jvm") version kotlinVersion
    java
    idea

    // Plugin which checks for dependency updates with help/dependencyUpdates task.
    id("com.github.ben-manes.versions") version "0.52.0"

    // Plugin which can update Gradle dependencies, use help/useLatestVersions
    id("se.patrikerdes.use-latest-versions") version "0.2.18"

    id("org.openjfx.javafxplugin") version "0.1.0"

    id("com.apollographql.apollo3") version "3.8.5"

    id("org.jlleitschuh.gradle.ktlint") version "12.2.0"
}

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://jetbrains.bintray.com/lets-plot-maven")
}

dependencies {
    implementation(kotlin("stdlib"))

    // Apollo and dependencies
    implementation("com.apollographql.apollo3:apollo-runtime:3.8.5")
    implementation("com.squareup.okio:okio:3.10.2")
    implementation("org.jetbrains:annotations:26.0.2")
    testImplementation("org.jetbrains:annotations:26.0.2")

    // Lets-plot
    implementation("org.openjfx:javafx-swing:25-ea+5")
    implementation("org.openjfx:javafx:25-ea+5")
    implementation("org.jetbrains.lets-plot:lets-plot-common:4.5.2")
    implementation("org.jetbrains.lets-plot:lets-plot-jfx:4.5.2")
    implementation("org.jetbrains.lets-plot:lets-plot-kotlin-jvm:4.9.3")
}

apollo {
    service("github") {
        packageName.set("nl.deltadak.texifystats")

        srcDir("src/main/graphql")
        includes.add("**/*.graphql")
        excludes.add("**/schema.graphql")
    }
}

javafx {
    modules("javafx.controls", "javafx.swing")
}

// Required by the GitHub Action
application {
    mainClass.set("nl.deltadak.texifystats.LetsPlotExample")
}

ktlint {
    filter {
        exclude { it.file.absolutePath.contains("/build/") }
    }
    verbose.set(true)
}

// https://github.com/ben-manes/gradle-versions-plugin
fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

tasks.dependencyUpdates {
    rejectVersionIf {
        isNonStable(candidate.version)
    }
}
