plugins {

    val kotlinVersion = "1.5.20"

    application
    kotlin("jvm") version kotlinVersion
    java
    idea

    // Plugin which checks for dependency updates with help/dependencyUpdates task.
    id("com.github.ben-manes.versions") version "0.42.0"

    // Plugin which can update Gradle dependencies, use help/useLatestVersions
    id("se.patrikerdes.use-latest-versions") version "0.2.18"

    id("org.openjfx.javafxplugin") version "0.0.13"

    id("com.apollographql.apollo3") version "3.5.0"

    id("org.jlleitschuh.gradle.ktlint") version "10.3.0"
}

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://jetbrains.bintray.com/lets-plot-maven")
}

dependencies {
    implementation(kotlin("stdlib"))

    // Apollo and dependencies
    implementation("com.apollographql.apollo3:apollo-runtime:3.5.0")
    implementation("com.squareup.okio:okio:3.2.0")
    implementation("org.jetbrains:annotations:23.0.0")
    testImplementation("org.jetbrains:annotations:23.0.0")

    // Lets-plot
    implementation("org.openjfx:javafx-swing:20-ea+1")
    implementation("org.openjfx:javafx:20-ea+1")
    implementation("org.jetbrains.lets-plot:lets-plot-common:2.4.0")
    implementation("org.jetbrains.lets-plot:lets-plot-jfx:2.4.0")
    implementation("org.jetbrains.lets-plot:lets-plot-kotlin-jvm:4.0.0")
}

apollo {
    packageName.set("nl.deltadak.texifystats")

    srcDir("src/main/graphql")
    includes.add("**/*.graphql")
    excludes.add("**/schema.graphql")
}

javafx {
    modules("javafx.controls", "javafx.swing")
}

// Required by the GitHub Action
application {
    mainClass.set("nl.deltadak.texifystats.LetsPlotExample")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

ktlint {
    filter {
        exclude { it.file.absolutePath.contains("/build/") }
    }
    verbose.set(true)
}
