import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.component.SoftwareComponentContainer
import org.gradle.api.plugins.PluginManager
import org.gradle.api.provider.Property
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPomLicense
import org.gradle.api.publish.maven.MavenPomScm
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.MavenPomDeploymentRepository
import org.gradle.api.tasks.Nested
import org.gradle.features.annotations.BindsProjectFeature
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition
import org.gradle.features.binding.ProjectFeatureApplicationContext
import org.gradle.features.binding.ProjectFeatureApplyAction
import org.gradle.features.binding.ProjectFeatureBinding
import org.gradle.features.binding.ProjectFeatureBindingBuilder
import org.gradle.features.dsl.bindProjectFeature
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.declarative.projecttypes.jvmapplication.JvmApplicationProjectType
import javax.inject.Inject

@BindsProjectFeature(MavenPublishFeature::class)
abstract class MavenPublishFeature : Plugin<Project>, ProjectFeatureBinding {
    override fun apply(target: Project) {}
    override fun bind(builder: ProjectFeatureBindingBuilder) {
        builder.bindProjectFeature("mavenPublish", ApplyAction::class)
            .withBuildModelImplementationType(DefaultMavenPublishBuildModel::class.java)
            .withUnsafeApplyAction()
    }

    internal abstract class ApplyAction : ProjectFeatureApplyAction<MavenPublishDefinition, MavenPublishBuildModel, JvmApplicationProjectType> {
        @get:Inject
        abstract val pluginManager: PluginManager

        @get:Inject
        abstract val project: Project

        @get:Inject abstract val components: SoftwareComponentContainer

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: MavenPublishDefinition,
            buildModel: MavenPublishBuildModel,
            parentDefinition: JvmApplicationProjectType,
        ) {
            pluginManager.apply("maven-publish")

            val publishing = project.extensions[PublishingExtension.NAME] as PublishingExtension
            val mavenPublication = publishing.publications.create<MavenPublication>("gpr") {
                from(components["java"])
            }
            (buildModel as DefaultMavenPublishBuildModel).mavenPublication = mavenPublication
            publishing.apply {
                publications.withType<MavenPublication>().configureEach {
                    pom {
                        name.set(definition.pom.name)
                        description.set(definition.pom.description)
                        url.set(definition.pom.url)
                        licenses {
                            license {
                                name.set(definition.pom.license.url)
                                url.set(definition.pom.license.url)
                            }
                        }
                        developers {
                            developer {
                                id.set(definition.pom.developer.id)
                                name.set(definition.pom.developer.name)
                                email.set(definition.pom.developer.email)
                            }
                        }
                        scm {
                            connection.set(definition.pom.scm.connection)
                            developerConnection.set(definition.pom.scm.developerConnection)
                            url.set(definition.pom.scm.url)
                        }

                        distributionManagement {
                            repository {
                                id.set(definition.pom.distributionManagement.repository.id)
                                name.set(definition.pom.distributionManagement.repository.name)
                                url.set(definition.pom.distributionManagement.repository.url)
                            }
                        }
                    }
                }
            }
        }
    }
}

interface MavenPublishDefinition : Definition<MavenPublishBuildModel> {
    @get:Nested
    val pom: MavenDclPom
}

interface MavenDclPom {
    val name: Property<String>
    val description: Property<String>
    val url: Property<String>
    @get:Nested
    val license: MavenPomLicense

    @get:Nested
    val developer: MavenDclPomDeveloper

    @get:Nested
    val scm: MavenPomScm

    @get:Nested
    val distributionManagement: MavenDclPomDistributionManagement
}

interface MavenDclPomDeveloper {
    val id: Property<String>
    val name: Property<String>
    val email: Property<String>
}

interface MavenDclPomDistributionManagement {
    @get:Nested
    val repository: MavenPomDeploymentRepository
}

interface MavenPublishBuildModel: BuildModel {
    val mavenPublication: MavenPublication
}

abstract class DefaultMavenPublishBuildModel : MavenPublishBuildModel {
    override lateinit var mavenPublication: MavenPublication
}
