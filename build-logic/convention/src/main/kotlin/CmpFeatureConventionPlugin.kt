import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/** Compose library + ViewModel and Koin-Compose, wired to the core modules every feature needs. */
class CmpFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("convention.cmp.library")

        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.apply {
                commonMain.dependencies {
                    implementation(project(":core:common"))
                    implementation(project(":core:domain"))
                    implementation(project(":core:ui"))
                    implementation(project(":core:designsystem"))
                    implementation(library("androidx-lifecycle-viewmodel"))
                    implementation(library("androidx-lifecycle-runtime"))
                    implementation(library("koin-compose-viewmodel"))
                    implementation(library("kotlinx-collections-immutable"))
                }
            }
        }
    }
}
