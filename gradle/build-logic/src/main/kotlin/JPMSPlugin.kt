import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Project.DEFAULT_VERSION
import org.gradle.api.internal.plugins.BindsProjectFeature
import org.gradle.api.internal.plugins.BuildModel
import org.gradle.api.internal.plugins.DeclaredProjectFeatureBindingBuilder
import org.gradle.api.internal.plugins.Definition
import org.gradle.api.internal.plugins.ProjectFeatureApplicationContext
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

@BindsProjectFeature(JPMSFeature::class)
abstract class JPMSFeature : Plugin<Project>, ProjectFeatureBinding {
    override fun apply(target: Project) {}
    override fun bind(builder: ProjectFeatureBindingBuilder) {
        builder.bindProjectFeature("jpms") { moduleName: JPMSDefinition, _: AdventOfCodeDefinition ->
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

interface JPMSDefinition : Definition<BuildModel.None> {
    val moduleName: Property<String>
}

// https://github.com/gradle/gradle/issues/35870
public inline fun <
        reified OwnDefinition : Definition<BuildModel.None>,
        reified TargetDefinition : Definition<*>,
        > ProjectFeatureBindingBuilder.bindProjectFeature(
    name: String,
    noinline block: ProjectFeatureApplicationContext.(OwnDefinition, TargetDefinition) -> Unit,
): DeclaredProjectFeatureBindingBuilder<OwnDefinition, BuildModel.None> =
    bindProjectFeature(name) { definition: OwnDefinition, _: BuildModel.None, target: TargetDefinition ->
        block(
            definition,
            target,
        )
    }
