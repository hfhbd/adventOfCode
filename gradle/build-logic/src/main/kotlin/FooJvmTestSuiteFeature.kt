import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.plugins.BindsProjectFeature
import org.gradle.api.internal.plugins.BuildModel
import org.gradle.api.internal.plugins.Definition
import org.gradle.api.internal.plugins.ProjectFeatureBinding
import org.gradle.api.internal.plugins.ProjectFeatureBindingBuilder

@BindsProjectFeature(FooJvmTestSuiteFeature::class)
abstract class FooJvmTestSuiteFeature : Plugin<Project>, ProjectFeatureBinding {
    override fun apply(target: Project) {}
    override fun bind(builder: ProjectFeatureBindingBuilder) {
        builder.bindProjectFeature("foo") { _: FooDefinition, _: JvmDclTestSuite ->

        }
    }
}

interface FooDefinition : Definition<BuildModel.None>
