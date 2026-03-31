import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.dsl.Dependencies
import org.gradle.api.artifacts.dsl.DependencyCollector
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.PluginManager
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import org.gradle.features.annotations.BindsProjectFeature
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition
import org.gradle.features.binding.ProjectFeatureApplicationContext
import org.gradle.features.binding.ProjectFeatureApplyAction
import org.gradle.features.binding.ProjectFeatureBinding
import org.gradle.features.binding.ProjectFeatureBindingBuilder
import org.gradle.features.dsl.bindProjectFeature
import org.gradle.features.dsl.bindProjectFeatureToBuildModel
import org.gradle.features.registration.TaskRegistrar
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.dokka.gradle.tasks.DokkaGenerateTask
import javax.inject.Inject

@BindsProjectFeature(DokkaFeature::class)
abstract class DokkaFeature : Plugin<Project>, ProjectFeatureBinding {
    override fun apply(target: Project) {}
    override fun bind(builder: ProjectFeatureBindingBuilder) {
        builder.bindProjectFeature("dokka", ApplyAggregationAction::class)
            .withUnsafeDefinition()
            .withUnsafeApplyAction()
        builder.bindProjectFeature("dokka", ApplyToKotlinJvmLibrary::class)
            .withUnsafeDefinition()
            .withUnsafeApplyAction()
        builder.bindProjectFeatureToBuildModel("dokka", DokkaDefinition::class, MavenPublishBuildModel::class, ApplyToMavenPublish::class)
            .withUnsafeDefinition()
            .withUnsafeApplyAction()
    }

    internal abstract class ApplyAggregationAction : ProjectFeatureApplyAction<DokkaDefinition, BuildModel.None, AggregationDefinition> {
        @get:Inject
        abstract val pluginManager: PluginManager

        @get:Inject
        abstract val configurations: ConfigurationContainer

        @get:Inject
        abstract val project: Project

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: DokkaDefinition,
            buildModel: BuildModel.None,
            parentDefinition: AggregationDefinition,
        ) {
            pluginManager.apply("org.jetbrains.dokka")
            val dokkaExtension = project.extensions.getByType<DokkaExtension>()
            configurations.getByName("dokka").dependencies.addAllLater(definition.dependencies.dokka.dependencies)
            project.afterEvaluate {
                dokkaExtension.dokkaPublications.configureEach {
                    includes.from(definition.publications.includes)
                }
            }
        }
    }


    internal abstract class ApplyToKotlinJvmLibrary : ProjectFeatureApplyAction<DokkaDefinition, BuildModel.None, KotlinJvmLibraryDefinition> {
        @get:Inject
        abstract val pluginManager: PluginManager

        @get:Inject
        abstract val project: Project

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: DokkaDefinition,
            buildModel: BuildModel.None,
            parentDefinition: KotlinJvmLibraryDefinition,
        ) {
            pluginManager.apply("org.jetbrains.dokka")

            val dokka = project.extensions.getByName("dokka") as DokkaExtension
            dokka.apply {
                for (dclSourceSet in definition.sourceSets) {
                    project.afterEvaluate {
                        dokkaSourceSets.named(dclSourceSet.name) {
                            reportUndocumented.set(dclSourceSet.reportUndocumented)
                            includes.from(dclSourceSet.includes)
                            sourceLink {
                                localDirectory.set(dclSourceSet.localDirectory)
                                remoteUrl.set(project.uri(dclSourceSet.remoteUrl.map {
                                    "$it${project.name}/src/main/kotlin"
                                }))
                                remoteLineSuffix.set(dclSourceSet.remoteLineSuffix)
                            }
                            samples.from(dclSourceSet.samples)
                        }
                    }
                }
            }
        }
    }

    internal abstract class ApplyToMavenPublish : ProjectFeatureApplyAction<DokkaDefinition, BuildModel.None, Definition<MavenPublishBuildModel>> {
        @get:Inject
        abstract val pluginManager: PluginManager

        @get:Inject abstract val taskRegistrar: TaskRegistrar

        @get:Inject
        abstract val project: Project

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: DokkaDefinition,
            buildModel: BuildModel.None,
            parentDefinition: Definition<MavenPublishBuildModel>,
        ) {
            pluginManager.apply("org.jetbrains.dokka-javadoc")

            // To generate documentation in Javadoc
            val dokkaJavadocJar = taskRegistrar.register("dokkaJavadocJar", Jar::class.java) {
                description = "A Javadoc JAR containing Dokka Javadoc"
                from(
                    project.tasks.named("dokkaGeneratePublicationJavadoc", DokkaGenerateTask::class)
                        .flatMap { it.outputDirectory })
                archiveClassifier.set("javadoc")
                group = JavaBasePlugin.DOCUMENTATION_GROUP
            }
            context.getBuildModel(parentDefinition).mavenPublication.artifact(dokkaJavadocJar)
        }
    }
}

interface DokkaDefinition : Definition<BuildModel.None> {
    @get:Nested
    val dependencies: DokkaDependencies

    @get:Nested
    val publications: DclDokkaPublication

    @get:Nested
    val sourceSets: NamedDomainObjectContainer<DclDokkaSourceSet>
}

interface DclDokkaPublication {
    val includes: ListProperty<RegularFile>
}

interface DclDokkaSourceSet : Named {
    val reportUndocumented: Property<Boolean>
    val includes: ListProperty<RegularFile>
    val samples: ListProperty<Directory>
    val localDirectory: DirectoryProperty
    val remoteUrl: Property<String>
    val remoteLineSuffix: Property<String>
}

interface DokkaDependencies : Dependencies {
    val dokka: DependencyCollector
}
