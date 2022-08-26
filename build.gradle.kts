plugins {
    kotlin("js") version "1.7.10"
}

allprojects {
    apply(plugin = "org.jetbrains.kotlin.js")
    repositories {
        mavenCentral()
    }

    kotlin {
        sourceSets.all {
            languageSettings.optIn("kotlin.ExperimentalStdlibApi")
            languageSettings.optIn("kotlin.RequiresOptIn")
        }
        js(IR) {
            browser {
                commonWebpackConfig {
                    sourceMaps = true
                    cssSupport.enabled = true
                }
            }
            binaries.executable()
        }
    }

    afterEvaluate {
        rootProject.extensions.configure<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension> {
            versions.webpackCli.version = "4.10.0"
        }
    }

    dependencies {
        implementation(enforcedPlatform("org.jetbrains.kotlin-wrappers:kotlin-wrappers-bom:1.0.0-pre.376"))
        api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
        api("org.jetbrains.kotlin-wrappers:kotlin-styled")
     //   implementation(npm("prop-types", "^15.6.2"))
    }
}
group = "moe.nea"
version = "0.0.1"