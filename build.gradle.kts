plugins {

    val kotlinVersion = "1.3.72"

    application
    kotlin("jvm") version kotlinVersion
    java
    idea

    // Plugin which checks for dependency updates with help/dependencyUpdates task.
    id("com.github.ben-manes.versions") version "0.28.0"

    // Plugin which can update Gradle dependencies, use help/useLatestVersions
    id("se.patrikerdes.use-latest-versions") version "0.2.14"

    id("org.openjfx.javafxplugin") version "0.0.8"

    id("com.apollographql.apollo") version "2.1.0"
}

dependencies {
    implementation(kotlin("stdlib"))

    // Apollo and dependencies
    implementation("com.apollographql.apollo:apollo-runtime:2.1.0")
    implementation("com.squareup.okio:okio:2.4.3")
    implementation("org.jetbrains:annotations:19.0.0")
    testImplementation("org.jetbrains:annotations:19.0.0")

    // Lets-plot
    implementation("org.openjfx:javafx-swing:15-ea+5")
    implementation("org.openjfx:javafx:15-ea+5")
    implementation("org.jetbrains.lets-plot:lets-plot-common:1.4.2")
    implementation("org.jetbrains.lets-plot:lets-plot-jfx:1.4.2")
    implementation("org.jetbrains.lets-plot:lets-plot-kotlin-api:0.0.9-SNAPSHOT")
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