import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Project.DEFAULT_VERSION
import org.gradle.features.annotations.BindsProjectFeature
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition
import org.gradle.features.binding.ProjectFeatureApplicationContext
import org.gradle.features.binding.ProjectFeatureBinding
import org.gradle.features.binding.ProjectFeatureBindingBuilder
import org.gradle.features.dsl.bindProjectFeature
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.features.binding.ProjectFeatureApplyAction
import org.gradle.kotlin.dsl.named
import org.gradle.process.CommandLineArgumentProvider
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import javax.inject.Inject

@BindsProjectFeature(JPMSFeature::class)
abstract class JPMSFeature : Plugin<Project>, ProjectFeatureBinding {
    override fun apply(target: Project) {}
    override fun bind(builder: ProjectFeatureBindingBuilder) {
        builder.bindProjectFeature("jpms", JPMSFeatureAction::class)
            .withUnsafeApplyAction()
    }

    internal interface JPMSFeatureAction : ProjectFeatureApplyAction<JPMSDefinition, BuildModel.None, AdventOfCodeDefinition> {
        @get:Inject
        val tasks: TaskContainer

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: JPMSDefinition,
            buildModel: BuildModel.None,
            parentDefinition: AdventOfCodeDefinition
        ) {
            tasks.named("compileJava", JavaCompile::class) {
                options.javaModuleVersion.set(project.version.toString().takeUnless { it == DEFAULT_VERSION })
                options.compilerArgumentProviders += object : CommandLineArgumentProvider {

                    @InputFiles
                    @PathSensitive(PathSensitivity.RELATIVE)
                    val kotlinClasses = project.tasks.named("compileKotlin", KotlinCompile::class)
                        .flatMap { it.destinationDirectory }

                    override fun asArguments(): List<String> = listOf(
                        "--patch-module",
                        "${definition.moduleName.get()}=${kotlinClasses.get().asFile.absolutePath}"
                    )
                }
            }
        }
    }
}

interface JPMSDefinition : Definition<BuildModel.None> {
    val moduleName: Property<String>
}
