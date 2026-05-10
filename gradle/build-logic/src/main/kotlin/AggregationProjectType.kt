import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.features.annotations.BindsProjectType
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition
import org.gradle.features.binding.ProjectFeatureApplicationContext
import org.gradle.features.binding.ProjectTypeApplyAction
import org.gradle.features.binding.ProjectTypeBinding
import org.gradle.features.binding.ProjectTypeBindingBuilder
import org.gradle.features.dsl.bindProjectType

@BindsProjectType(AggregationProjectType::class)
abstract class AggregationProjectType : Plugin<Project>, ProjectTypeBinding {
    override fun apply(target: Project) {}
    override fun bind(builder: ProjectTypeBindingBuilder) {
        builder.bindProjectType("aggregate", ApplyAction::class)
    }
    abstract class ApplyAction : ProjectTypeApplyAction<AggregationDefinition, BuildModel.None> {
        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: AggregationDefinition,
            buildModel: BuildModel.None
        ) {

        }
    }
}

interface AggregationDefinition : Definition<BuildModel.None>
