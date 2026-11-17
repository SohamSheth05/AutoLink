import androidx.room.gradle.RoomExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/** KSP + Room KMP for every target this project builds (Android + desktop JVM). Used by core:database. */
class RoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("convention.kmp.library")
        pluginManager.apply("com.google.devtools.ksp")
        pluginManager.apply("androidx.room")

        extensions.configure<RoomExtension> {
            // Exported schemas are committed: they are the input for future migration tests.
            schemaDirectory("$projectDir/schemas")
        }

        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.apply {
                commonMain.dependencies {
                    implementation(library("room-runtime"))
                    implementation(library("sqlite-bundled"))
                }
            }
        }

        dependencies {
            add("kspAndroid", library("room-compiler"))
            add("kspDesktop", library("room-compiler"))
        }
    }
}
