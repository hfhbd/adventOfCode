import dev.detekt.gradle.Detekt
import dev.detekt.gradle.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Usage
import org.gradle.api.plugins.PluginManager
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.TaskContainer
import org.gradle.features.annotations.BindsProjectFeature
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition
import org.gradle.features.binding.ProjectFeatureApplicationContext
import org.gradle.features.binding.ProjectFeatureApplyAction
import org.gradle.features.binding.ProjectFeatureBinding
import org.gradle.features.binding.ProjectFeatureBindingBuilder
import org.gradle.features.dsl.bindProjectFeature
import org.gradle.features.registration.ConfigurationRegistrar
import org.gradle.features.registration.TaskRegistrar
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.named
import javax.inject.Inject
import org.jetbrains.kotlin.gradle.declarative.projecttypes.jvmapplication.JvmApplicationProjectType

@BindsProjectFeature(DetektFeature::class)
abstract class DetektFeature : Plugin<Project>, ProjectFeatureBinding {
    override fun apply(target: Project) {}
    override fun bind(builder: ProjectFeatureBindingBuilder) {
        builder.bindProjectFeature("detekt", ApplyAction::class)
            .withUnsafeApplyAction()
    }

    internal abstract class ApplyAction : ProjectFeatureApplyAction<DetektDefinition, BuildModel.None, JvmApplicationProjectType> {
        @get:Inject
        abstract val pluginManager: PluginManager

        @get:Inject
        abstract val project: Project

        @get:Inject
        abstract val providers: ProviderFactory

        @get:Inject
        abstract val taskRegistrar: TaskRegistrar

        @get:Inject
        abstract val configurationRegistrar: ConfigurationRegistrar

        @get:Inject
        abstract val tasks: TaskContainer

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: DetektDefinition,
            buildModel: BuildModel.None,
            parentDefinition: JvmApplicationProjectType,
        ) {
            pluginManager.apply("dev.detekt")
            val detekt = project.extensions["detekt"] as DetektExtension
            detekt.apply {
                parallel.set(true)
                autoCorrect.set(true)
                buildUponDefaultConfig.set(true)
                ignoreFailures.set(providers.gradleProperty("ignoreDetektFailures").map { it.toBoolean() }
                    .orElse(false))
            }

            taskRegistrar.register("deleteDetektBaseline", Delete::class.java) {
                delete(project.tasks.named("detekt", Detekt::class).flatMap { it.baseline })
            }

            configurationRegistrar.consumable("sarif") {
                attributes {
                    attribute(Usage.USAGE_ATTRIBUTE, named("detekt-sarif"))
                }
                outgoing {
                    artifact(
                        tasks.named("detekt", Detekt::class).flatMap { it.reports.sarif.outputLocation })
                }
            }
        }
    }
}

interface DetektDefinition : Definition<BuildModel.None>
