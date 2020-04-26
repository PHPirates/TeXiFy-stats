[![Build Status](https://travis-ci.com/PHPirates/TeXiFy-stats.svg?branch=master)](https://travis-ci.com/PHPirates/TeXiFy-stats)
![](https://github.com/PHPirates/texify-stats/workflows/GitHub%20Action/badge.svg?branch=master)

# TeXiFy-stats: statistics for TeXiFy-IDEA

This project uses
* Kotlin
* Gradle Kotlin DSL
* Apollo-Android
* Lets-plot via [lets-plot-kotlin](https://github.com/JetBrains/lets-plot-kotlin), [lets-plot documentation](https://htmlpreview.github.io/?https://github.com/JetBrains/lets-plot-kotlin/blob/master/plot-api/docs/plot-api/index.html)

The JS GraphQL IntelliJ plugin is recommended.

# Setup

Documentation about the setup of the Apollo part of the project: https://stackoverflow.com/questions/59776117/how-to-use-apollo-android-from-kotlin-but-without-android

This project requires Java 11 or later, the following instructions have been tested for Java 13.
* Make sure you have java-openjfx 13 and jdk-openjdk 13 installed.
* In File > Project Structure > Project change the Project SDK and language level both to 13.
* In File > Settings > Build, Execution, Deployment > Build Tools > Gradle change Gradle JVM to 13.
* Run the apollo > generateApolloClasses Gradle task
* Generate a [GitHub token](https://github.com/settings/tokens) and add it as a program argument of the run config

# Currently available plots

![](figures/lineplot-total-issues.png)
![](figures/lineplot-total-prs.png)
![](figures/histogram-creation-dates.png)