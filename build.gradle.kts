plugins {

    val kotlinVersion = "1.3.72"

    application
    kotlin("jvm") version kotlinVersion
    java
    idea

    // Plugin which checks for dependency updates with help/dependencyUpdates task.
    id("com.github.ben-manes.versions") version "0.36.0"

    // Plugin which can update Gradle dependencies, use help/useLatestVersions
    id("se.patrikerdes.use-latest-versions") version "0.2.15"

    id("org.openjfx.javafxplugin") version "0.0.9"

    id("com.apollographql.apollo") version "2.5.2"
}

dependencies {
    implementation(kotlin("stdlib"))

    // Apollo and dependencies
    implementation("com.apollographql.apollo:apollo-runtime:2.5.2")
    implementation("com.squareup.okio:okio:2.4.3")
    implementation("org.jetbrains:annotations:20.1.0")
    testImplementation("org.jetbrains:annotations:20.1.0")

    // Lets-plot
    implementation("org.openjfx:javafx-swing:16-ea+6")
    implementation("org.openjfx:javafx:16-ea+6")
    implementation("org.jetbrains.lets-plot:lets-plot-common:1.5.6")
    implementation("org.jetbrains.lets-plot:lets-plot-jfx:1.5.6")
    implementation("org.jetbrains.lets-plot:lets-plot-kotlin-api:1.0.0-rc1")
}

repositories {
    mavenCentral()
    mavenLocal()
    jcenter()
    maven("https://jetbrains.bintray.com/lets-plot-maven")
}

apollo {
    generateKotlinModels.set(true)
    graphqlSourceDirectorySet.srcDir("src/main/graphql")
    graphqlSourceDirectorySet.include("**/*.graphql")
    graphqlSourceDirectorySet.exclude("**/schema.graphql")
}

javafx {
    modules("javafx.controls", "javafx.swing")
}

// Required by the GitHub Action
application {
    mainClassName = "nl.deltadak.texifystats.LetsPlotExample"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}