plugins {
    kotlin("multiplatform") version "1.7.10"
    `maven-publish`
}

repositories {
    mavenCentral()
}
group = "moe.nea"
version = "0.0.1"

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
    sourceSets {
        named("jsMain") {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
                api("org.jetbrains.kotlin-wrappers:kotlin-styled:5.3.5-pre.376")
            }
        }
    }
}


publishing {
    repositories {
        mavenLocal()
    }
    publications {
        create<MavenPublication>("library") {
            from(components["kotlin"])
        }
    }
}

