import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.features.annotations.BindsProjectFeature
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition
import org.gradle.features.binding.ProjectFeatureBinding
import org.gradle.features.binding.ProjectFeatureBindingBuilder
import org.gradle.features.dsl.bindProjectFeature

@BindsProjectFeature(KotlinTestJvmTestSuiteFeature::class)
abstract class KotlinTestJvmTestSuiteFeature : Plugin<Project>, ProjectFeatureBinding {
    override fun apply(target: Project) {}

    override fun bind(builder: ProjectFeatureBindingBuilder) {
        builder.bindProjectFeature("useKotlinTest") { _: UseKotlinTestDefinition, _: BuildModel.None, jvmDclTestSuite: JvmDclTestSuite ->
            getBuildModel(jvmDclTestSuite).testSuite.useKotlinTest()
        }
    }
}

interface UseKotlinTestDefinition : Definition<BuildModel.None>
