import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Plain KMP library. AutoLink ships only on Android Automotive, so the targets are Android plus a
 * JVM "desktop" target that is never shipped: it runs the fast common tests and the Koin verify() check.
 * iOS is deliberately left out (no AAOS equivalent).
 */
class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("org.jetbrains.kotlin.multiplatform")
            apply("com.android.kotlin.multiplatform.library")
            apply("org.jetbrains.kotlin.plugin.serialization")
        }

        extensions.configure<KotlinMultiplatformExtension> {
            jvmToolchain(17)

            (this as ExtensionAware).extensions.configure<KotlinMultiplatformAndroidLibraryExtension> {
                namespace = pathNamespace
                compileSdk = versionInt("android-compileSdk")
                minSdk = versionInt("android-minSdk")
            }
            jvm("desktop")

            compilerOptions {
                freeCompilerArgs.add("-Xexpect-actual-classes")
                optIn.add("kotlin.time.ExperimentalTime")
            }

            sourceSets.apply {
                commonMain.dependencies {
                    implementation(library("kotlinx-coroutines-core"))
                    implementation(library("kotlinx-serialization-json"))
                    implementation(project.dependencies.platform(library("koin-bom")))
                    implementation(library("koin-core"))
                }
                commonTest.dependencies {
                    implementation(kotlin("test"))
                    implementation(library("kotlinx-coroutines-test"))
                    implementation(library("turbine"))
                }
            }
        }
    }
}
