import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.testing.toolchains.internal.KotlinTestTestToolchain
import org.gradle.features.annotations.BindsProjectFeature
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition
import org.gradle.features.binding.ProjectFeatureApplicationContext
import org.gradle.features.binding.ProjectFeatureApplyAction
import org.gradle.features.binding.ProjectFeatureBinding
import org.gradle.features.binding.ProjectFeatureBindingBuilder
import org.gradle.features.dsl.bindProjectFeature

@BindsProjectFeature(KotlinTestJvmTestSuiteFeature::class)
abstract class KotlinTestJvmTestSuiteFeature : Plugin<Project>, ProjectFeatureBinding {
    override fun apply(target: Project) {}

    override fun bind(builder: ProjectFeatureBindingBuilder) {
        builder.bindProjectFeature("useKotlinTest", ApplyAction::class)
    }

    abstract class ApplyAction : ProjectFeatureApplyAction<UseKotlinTestDefinition, BuildModel.None, JvmDclTestSuite> {
        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: UseKotlinTestDefinition,
            buildModel: BuildModel.None,
            parentDefinition: JvmDclTestSuite
        ) {
            context.getBuildModel(parentDefinition).testSuite.useKotlinTest(
                definition.version.orElse(KotlinTestTestToolchain.DEFAULT_VERSION)
            )
        }
    }
}

interface UseKotlinTestDefinition : Definition<BuildModel.None> {
    val version: Property<String>
}
