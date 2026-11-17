plugins {
    alias(libs.plugins.convention.kmp.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.common)
            implementation(projects.core.domain)
            implementation(projects.core.database)
            // Room types (RoomDatabase.Builder) appear in tests that build an in-memory database.
            implementation(libs.room.runtime)
        }
    }
}
