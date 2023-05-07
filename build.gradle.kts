plugins {

    val kotlinVersion = "1.8.20"

    application
    kotlin("jvm") version kotlinVersion
    java
    idea

    // Plugin which checks for dependency updates with help/dependencyUpdates task.
    id("com.github.ben-manes.versions") version "0.46.0"

    // Plugin which can update Gradle dependencies, use help/useLatestVersions
    id("se.patrikerdes.use-latest-versions") version "0.2.18"

    id("org.openjfx.javafxplugin") version "0.0.14"

    id("com.apollographql.apollo3") version "3.8.1"

    id("org.jlleitschuh.gradle.ktlint") version "11.3.2"
}

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://jetbrains.bintray.com/lets-plot-maven")
}

dependencies {
    implementation(kotlin("stdlib"))

    // Apollo and dependencies
    implementation("com.apollographql.apollo3:apollo-runtime:3.8.1")
    implementation("com.squareup.okio:okio:3.3.0")
    implementation("org.jetbrains:annotations:24.0.1")
    testImplementation("org.jetbrains:annotations:24.0.1")

    // Lets-plot
    implementation("org.openjfx:javafx-swing:21-ea+5")
    implementation("org.openjfx:javafx:21-ea+5")
    implementation("org.jetbrains.lets-plot:lets-plot-common:3.2.0-rc1")
    implementation("org.jetbrains.lets-plot:lets-plot-jfx:3.2.0-rc1")
    implementation("org.jetbrains.lets-plot:lets-plot-kotlin-jvm:4.3.0")
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
