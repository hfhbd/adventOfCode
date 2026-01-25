import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.initialization.resolve.RepositoriesMode
import org.gradle.api.internal.plugins.software.RegistersSoftwareTypes

@RegistersSoftwareTypes(
    AdventOfCodeProjectType::class,

    JPMSFeature::class,
    JvmTestSuiteFeature::class,
)
abstract class MyReposPlugin : Plugin<Settings> {
    override fun apply(target: Settings) {
        target.pluginManager.apply("org.gradle.toolchains.foojay-resolver-convention")

        target.dependencyResolutionManagement {
            repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
            repositories {
                mavenCentral()
            }
        }
    }
}
