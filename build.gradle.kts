plugins {

    val kotlinVersion = "1.5.20"

    application
    kotlin("jvm") version kotlinVersion
    java
    idea

    // Plugin which checks for dependency updates with help/dependencyUpdates task.
    id("com.github.ben-manes.versions") version "0.36.0"

    // Plugin which can update Gradle dependencies, use help/useLatestVersions
    id("se.patrikerdes.use-latest-versions") version "0.2.15"

    id("org.openjfx.javafxplugin") version "0.0.9"

    id("com.apollographql.apollo3") version "3.0.0-alpha02"

    id("org.jlleitschuh.gradle.ktlint") version "10.0.0"
}

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://jetbrains.bintray.com/lets-plot-maven")
}

dependencies {
    implementation(kotlin("stdlib"))

    // Apollo and dependencies
    implementation("com.apollographql.apollo3:apollo-runtime:3.0.0-alpha02")
    implementation("com.squareup.okio:okio:2.4.3")
    implementation("org.jetbrains:annotations:20.1.0")
    testImplementation("org.jetbrains:annotations:20.1.0")

    // Lets-plot
    implementation("org.openjfx:javafx-swing:16-ea+6")
    implementation("org.openjfx:javafx:16-ea+6")
    implementation("org.jetbrains.lets-plot:lets-plot-common:2.0.4")
    implementation("org.jetbrains.lets-plot:lets-plot-jfx:2.0.4")
    implementation("org.jetbrains.lets-plot:lets-plot-kotlin-jvm:3.0.1")
}

apollo {
    srcDir("src/main/graphql")
    include.add("**/*.graphql")
    exclude.add("**/schema.graphql")
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
