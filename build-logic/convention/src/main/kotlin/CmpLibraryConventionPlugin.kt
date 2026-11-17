import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/** KMP library + Compose Multiplatform. Used by core:designsystem, core:ui. */
class CmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("convention.kmp.library")
        pluginManager.apply("org.jetbrains.compose")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.apply {
                commonMain.dependencies {
                    implementation(library("compose-runtime"))
                    implementation(library("compose-foundation"))
                    implementation(library("compose-ui"))
                    implementation(library("compose-material3"))
                    implementation(library("koin-compose"))
                }
            }
        }
    }
}
