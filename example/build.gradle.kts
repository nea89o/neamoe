plugins {
    kotlin("js")
}

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


dependencies {
    implementation(npm("@fontsource/comic-mono", "^4.5.0"))
    implementation(rootProject)
}