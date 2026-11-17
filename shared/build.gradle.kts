// Composition root: App() and the Koin graph over every module.
plugins {
    alias(libs.plugins.convention.cmp.feature)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.data)
            implementation(projects.core.database)
            implementation(projects.feature.vehicle)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
        }
        named("desktopTest").dependencies {
            implementation(libs.koin.test)
        }
    }
}
