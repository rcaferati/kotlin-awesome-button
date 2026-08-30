import com.android.build.api.dsl.ManagedVirtualDevice
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.vanniktech.maven.publish")
    id("org.jetbrains.dokka")
    id("org.jetbrains.kotlinx.kover")
    id("org.jlleitschuh.gradle.ktlint")
}

android {
    namespace = "dev.caferati.awesomebutton"
    compileSdk = 36

    defaultConfig {
        minSdk = 23
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    testOptions {
        targetSdk = 36
        managedDevices {
            allDevices {
                create<ManagedVirtualDevice>("pixel2Api35") {
                    device = "Pixel 2"
                    apiLevel = 35
                    systemImageSource = "aosp-atd"
                }
            }
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    explicitApi()
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

composeCompiler {
    metricsDestination.set(layout.buildDirectory.dir("compose_compiler/metrics"))
    reportsDestination.set(layout.buildDirectory.dir("compose_compiler/reports"))
}

dokka {
    dokkaPublications.html {
        failOnWarning.set(true)
    }
    dokkaSourceSets.configureEach {
        documentedVisibilities.set(setOf(VisibilityModifier.Public))
        reportUndocumented.set(true)
    }
}

ktlint {
    version.set("1.5.0")
    android.set(true)
    ignoreFailures.set(false)
}

kover {
    reports {
        variant("debug") {
            xml {
                xmlFile.set(layout.buildDirectory.file("reports/kover/debug/report.xml"))
            }
            html {
                htmlDir.set(layout.buildDirectory.dir("reports/kover/debug/html"))
            }
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.02.01")

    api(composeBom)
    api("androidx.compose.animation:animation")
    api("androidx.compose.foundation:foundation")
    api("androidx.compose.runtime:runtime")
    api("androidx.compose.ui:ui")

    testImplementation("junit:junit:4.13.2")

    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.activity:activity-compose:1.13.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

val isPublishingToMavenCentral =
    gradle.startParameter.taskNames.any {
        it.contains("MavenCentral", ignoreCase = true)
    }
val hasSigningConfiguration =
    providers.gradleProperty("signingInMemoryKey").isPresent ||
        providers.gradleProperty("signing.secretKeyRingFile").isPresent

mavenPublishing {
    publishToMavenCentral()
    if (isPublishingToMavenCentral || hasSigningConfiguration) {
        signAllPublications()
    }
}
