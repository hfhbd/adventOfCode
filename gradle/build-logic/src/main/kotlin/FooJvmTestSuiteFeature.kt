import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.features.annotations.BindsProjectFeature
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition
import org.gradle.features.binding.ProjectFeatureBinding
import org.gradle.features.binding.ProjectFeatureBindingBuilder
import org.gradle.features.dsl.bindProjectFeature

@BindsProjectFeature(FooJvmTestSuiteFeature::class)
abstract class FooJvmTestSuiteFeature : Plugin<Project>, ProjectFeatureBinding {
    override fun apply(target: Project) {}
    override fun bind(builder: ProjectFeatureBindingBuilder) {
        builder.bindProjectFeature("foo") { _: FooDefinition, _, _: JvmDclTestSuite ->

        }
    }
}

interface FooDefinition : Definition<BuildModel.None>
