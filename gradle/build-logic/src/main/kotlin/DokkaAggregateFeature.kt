import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.dsl.Dependencies
import org.gradle.api.artifacts.dsl.DependencyCollector
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.plugins.PluginManager
import org.gradle.api.tasks.Nested
import org.gradle.features.annotations.BindsProjectFeature
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition
import org.gradle.features.binding.ProjectFeatureApplicationContext
import org.gradle.features.binding.ProjectFeatureApplyAction
import org.gradle.features.binding.ProjectFeatureBinding
import org.gradle.features.binding.ProjectFeatureBindingBuilder
import org.gradle.features.dsl.bindProjectFeature
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.dokka.gradle.DokkaExtension
import javax.inject.Inject

@BindsProjectFeature(DokkaAggregateFeature::class)
abstract class DokkaAggregateFeature : Plugin<Project>, ProjectFeatureBinding {
    override fun apply(target: Project) {}
    override fun bind(builder: ProjectFeatureBindingBuilder) {
        builder.bindProjectFeature("dokka", ApplyAction::class)
            .withUnsafeDefinition()
            .withUnsafeApplyAction()
    }

    internal abstract class ApplyAction : ProjectFeatureApplyAction<DokkaAggregateDefinition, BuildModel.None, AggregationDefinition> {
        @get:Inject
        abstract val pluginManager: PluginManager

        @get:Inject
        abstract val configurations: ConfigurationContainer

        @get:Inject
        abstract val project: Project

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: DokkaAggregateDefinition,
            buildModel: BuildModel.None,
            parentDefinition: AggregationDefinition
        ) {
            pluginManager.apply("org.jetbrains.dokka")
            val dokkaExtension = project.extensions.getByType<DokkaExtension>()
            configurations.getByName("dokka").dependencies.addAllLater(definition.dependencies.dokka.dependencies)
            definition.dokkaPublications.all {
                dokkaExtension.dokkaPublications.named(name) {
                    includes.from(this@all.includes)
                }
            }
        }
    }
}

interface DokkaAggregateDefinition : Definition<BuildModel.None> {
    @get:Nested
    val dependencies: DokkaDependencies

    @get:Nested
    val dokkaPublications: NamedDomainObjectContainer<DclDokkaPublication>
}

interface DclDokkaPublication : Named {
    val includes: ConfigurableFileCollection
}

interface DokkaDependencies : Dependencies {
    val dokka: DependencyCollector
}
