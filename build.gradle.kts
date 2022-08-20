plugins {
    kotlin("js") version "1.6.0"
    id("com.github.node-gradle.node") version "3.1.1"
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
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0-RC")

        implementation(enforcedPlatform("org.jetbrains.kotlin-wrappers:kotlin-wrappers-bom:0.0.1-pre.256-kotlin-1.5.31"))
        implementation("org.jetbrains.kotlin-wrappers:kotlin-styled")
     //   implementation(npm("prop-types", "^15.6.2"))
    }
}
group = "moe.nea"
version = "0.0.1"