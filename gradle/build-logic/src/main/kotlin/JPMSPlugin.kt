import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Project.DEFAULT_VERSION
import org.gradle.api.internal.plugins.BindsProjectFeature
import org.gradle.api.internal.plugins.BuildModel
import org.gradle.api.internal.plugins.Definition
import org.gradle.api.internal.plugins.ProjectFeatureBinding
import org.gradle.api.internal.plugins.ProjectFeatureBindingBuilder
import org.gradle.api.internal.plugins.features.dsl.bindProjectFeature
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.named
import org.gradle.process.CommandLineArgumentProvider
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@BindsProjectFeature(JPMSPlugin.Binding::class)
abstract class JPMSPlugin: Plugin<Project> {
    override fun apply(target: Project) {}
    class Binding : ProjectFeatureBinding {
        override fun bind(builder: ProjectFeatureBindingBuilder) {
            builder.bindProjectFeature("jpms") { moduleName: JPMSDefinition, _: BuildModel.None, _: AdventOfCodeDefinition ->
                project.tasks.named("compileJava", JavaCompile::class) {
                    options.javaModuleVersion.set(project.version.toString().takeUnless { it == DEFAULT_VERSION })
                    options.compilerArgumentProviders += object : CommandLineArgumentProvider {

                        @InputFiles
                        @PathSensitive(PathSensitivity.RELATIVE)
                        val kotlinClasses = project.tasks.named("compileKotlin", KotlinCompile::class)
                            .flatMap { it.destinationDirectory }

                        override fun asArguments(): List<String> = listOf(
                            "--patch-module",
                            "${moduleName.moduleName.get()}=${kotlinClasses.get().asFile.absolutePath}"
                        )
                    }
                }
            }
        }
    }
}

interface JPMSDefinition : Definition<BuildModel.None> {
    val moduleName: Property<String>
}
