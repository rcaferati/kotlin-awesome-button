plugins {
    id("com.android.application") version "8.13.2" apply false
    id("com.android.library") version "8.13.2" apply false
    id("org.jetbrains.kotlin.android") version "2.2.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.21" apply false
    id("com.vanniktech.maven.publish") version "0.36.0" apply false
    id("org.jetbrains.dokka") version "2.2.0" apply false
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.18.1"
    id("org.jetbrains.kotlinx.kover") version "0.9.9" apply false
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0" apply false
}

apiValidation {
    ignoredProjects.add("demo")
}
