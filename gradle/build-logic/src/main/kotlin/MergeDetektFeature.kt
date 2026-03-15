import dev.detekt.gradle.report.ReportMergeTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.Dependencies
import org.gradle.api.artifacts.dsl.DependencyCollector
import org.gradle.api.attributes.Usage
import org.gradle.api.tasks.Nested
import org.gradle.features.annotations.BindsProjectFeature
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition
import org.gradle.features.binding.ProjectFeatureApplicationContext
import org.gradle.features.binding.ProjectFeatureApplyAction
import org.gradle.features.binding.ProjectFeatureBinding
import org.gradle.features.binding.ProjectFeatureBindingBuilder
import org.gradle.features.dsl.bindProjectFeature
import org.gradle.features.file.ProjectFeatureLayout
import org.gradle.features.registration.ConfigurationRegistrar
import org.gradle.features.registration.TaskRegistrar
import org.gradle.kotlin.dsl.named
import javax.inject.Inject

@BindsProjectFeature(MergeDetektFeature::class)
abstract class MergeDetektFeature : Plugin<Project>, ProjectFeatureBinding {
    override fun apply(target: Project) {}
    override fun bind(builder: ProjectFeatureBindingBuilder) {
        builder.bindProjectFeature("mergeDetekt", ApplyAction::class)
            // https://github.com/gradle/gradle/issues/36755
            .withUnsafeDefinition()
            .withUnsafeApplyAction()
    }

    abstract class ApplyAction : ProjectFeatureApplyAction<MergeDetektDefinition, BuildModel.None, AggregationDefinition> {
        @get:Inject
        abstract val configurations: ConfigurationRegistrar

        @get:Inject
        abstract val tasks: TaskRegistrar

        @get:Inject
        abstract val layout: ProjectFeatureLayout

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: MergeDetektDefinition,
            buildModel: BuildModel.None,
            parentDefinition: AggregationDefinition
        ) {
            val sarifFiles = configurations.resolvable("sarifFiles") {
                fromDependencyCollector(definition.dependencies.sarif)
                attributes {
                    attribute(Usage.USAGE_ATTRIBUTE, named("detekt-sarif"))
                }
            }

            tasks.register("mergeDetektSarif", ReportMergeTask::class.java) {
                input.from(sarifFiles)
                output.set(layout.contextBuildDirectory.map { it.file("reports/detekt/detekt.sarif") })
            }
        }
    }
}

interface MergeDetektDefinition : Definition<BuildModel.None> {
    @get:Nested
    val dependencies: SarifDependencies
}

interface SarifDependencies : Dependencies {
    val sarif: DependencyCollector
}
