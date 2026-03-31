import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.PasswordCredentials
import org.gradle.api.plugins.PluginManager
import org.gradle.api.publish.PublishingExtension
import org.gradle.features.annotations.BindsProjectFeature
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition
import org.gradle.features.binding.ProjectFeatureApplicationContext
import org.gradle.features.binding.ProjectFeatureApplyAction
import org.gradle.features.binding.ProjectFeatureBinding
import org.gradle.features.binding.ProjectFeatureBindingBuilder
import org.gradle.features.dsl.bindProjectFeature
import org.gradle.kotlin.dsl.credentials
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.maven
import org.gradle.plugins.signing.SigningExtension
import javax.inject.Inject

@BindsProjectFeature(PublishToMavenCentralFeature::class)
abstract class PublishToMavenCentralFeature : Plugin<Project>, ProjectFeatureBinding {
    override fun apply(target: Project) {}
    override fun bind(builder: ProjectFeatureBindingBuilder) {
        builder.bindProjectFeature("publishToMavenCentral", ApplyAction::class)
            .withUnsafeApplyAction()
    }

    internal abstract class ApplyAction : ProjectFeatureApplyAction<PublishToMavenCentralDefinition, BuildModel.None, MavenPublishDefinition> {
        @get:Inject
        abstract val pluginManager: PluginManager

        @get:Inject
        abstract val project: Project

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: PublishToMavenCentralDefinition,
            buildModel: BuildModel.None,
            parentDefinition: MavenPublishDefinition,
        ) {
            pluginManager.apply("signing")
            pluginManager.apply("io.github.hfhbd.mavencentral")
            pluginManager.apply("dev.sigstore.sign")

            val publishing = project.extensions[PublishingExtension.NAME] as PublishingExtension
            publishing.apply {
                repositories {
                    maven(url = "https://central.sonatype.com/repository/maven-snapshots/") {
                        name = "mavenCentralSnapshot"
                        credentials(PasswordCredentials::class)
                    }
                }
            }

            val signing = project.extensions.getByName("signing") as SigningExtension
            signing.apply {
                useInMemoryPgpKeys(
                    project.providers.gradleProperty("signingKey").orNull,
                    project.providers.gradleProperty("signingPassword").orNull,
                )
                isRequired = project.providers.gradleProperty("signingKey").isPresent
                sign(publishing.publications)
            }
        }
    }
}

interface PublishToMavenCentralDefinition : Definition<BuildModel.None>
