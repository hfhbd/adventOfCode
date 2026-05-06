import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
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
import javax.inject.Inject

@BindsProjectFeature(TestFixturesFeature::class)
abstract class TestFixturesFeature : Plugin<Project>, ProjectFeatureBinding {
    override fun apply(target: Project) {}
    override fun bind(builder: ProjectFeatureBindingBuilder) {
        builder.bindProjectFeature("testFixtures", ApplyAction::class)
            .withUnsafeApplyAction()
            // https://github.com/gradle/gradle/issues/36755
            .withUnsafeDefinition()
    }

    abstract class ApplyAction @Inject constructor(
        private val pluginManager: PluginManager,
        private val configurations: ConfigurationContainer,
    ) : ProjectFeatureApplyAction<TestFixturesDefinition, BuildModel.None, org.jetbrains.kotlin.gradle.declarative.projecttypes.jvmapplication.JvmApplicationProjectType> {


        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: TestFixturesDefinition,
            buildModel: BuildModel.None,
            parentDefinition: org.jetbrains.kotlin.gradle.declarative.projecttypes.jvmapplication.JvmApplicationProjectType,
        ) {
            pluginManager.apply("java-test-fixtures")
            configurations.wire("testFixtures", definition.dependencies)
        }
    }
}

interface TestFixturesDefinition : Definition<BuildModel.None> {
    @get:Nested
    val dependencies: JvmLibraryDependencies
}
