import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/** Android app module (AGP 9 built-in Kotlin) with Compose. Used by androidApp. */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        extensions.configure<ApplicationExtension> {
            compileSdk = versionInt("android-compileSdk")
            defaultConfig {
                minSdk = versionInt("android-minSdk")
                targetSdk = versionInt("android-targetSdk")
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
            buildFeatures {
                compose = true
                buildConfig = true
            }
            packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
        }
    }
}
