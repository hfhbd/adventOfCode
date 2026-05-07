import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.initialization.resolve.RepositoriesMode
import org.gradle.features.annotations.RegistersProjectFeatures

@RegistersProjectFeatures(
    AggregationProjectType::class,

    JPMSFeature::class,
    JvmTestSuiteFeature::class,
    TestFixturesFeature::class,

    MergeDetektFeature::class,
    DokkaFeature::class,
    DetektFeature::class,

    MavenPublishFeature::class,
    PublishToMavenCentralFeature::class,
    PublishToGitHubPackagesFeature::class,

    KotlinTestJvmTestSuiteFeature::class
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
