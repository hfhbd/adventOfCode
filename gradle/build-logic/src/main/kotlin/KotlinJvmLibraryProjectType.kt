import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.dsl.Dependencies
import org.gradle.api.artifacts.dsl.DependencyCollector
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.plugins.PluginManager
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.features.annotations.BindsProjectType
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition
import org.gradle.features.binding.ProjectFeatureApplicationContext
import org.gradle.features.binding.ProjectTypeApplyAction
import org.gradle.features.binding.ProjectTypeBinding
import org.gradle.features.binding.ProjectTypeBindingBuilder
import org.gradle.features.dsl.bindProjectType
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.get
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import javax.inject.Inject

@BindsProjectType(KotlinJvmLibraryProjectType::class)
abstract class KotlinJvmLibraryProjectType : Plugin<Project>, ProjectTypeBinding {
    override fun apply(target: Project) {}
    override fun bind(builder: ProjectTypeBindingBuilder) {
        builder.bindProjectType("kotlinJvmLibrary", ApplyAction::class)
            .withUnsafeDefinition()
            .withBuildModelImplementationType(DefaultKotlinJvmLibraryBuildModel::class.java)
            .withUnsafeApplyAction()
    }

    internal abstract class ApplyAction : ProjectTypeApplyAction<KotlinJvmLibraryDefinition, KotlinJvmLibraryBuildModel> {
        @get:Inject abstract val pluginManager: PluginManager

        @get:Inject abstract val configurations: ConfigurationContainer

        @get:Inject abstract val project: Project
        @get:Inject abstract val tasks: TaskContainer

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: KotlinJvmLibraryDefinition,
            buildModel: KotlinJvmLibraryBuildModel,
        ) {
            pluginManager.apply("org.jetbrains.kotlin.jvm")
            pluginManager.apply("java-test-fixtures")

            buildModel as DefaultKotlinJvmLibraryBuildModel
            buildModel.jvmToolchain = definition.jvmToolchain
            buildModel.compileJavaTask = tasks.named("compileJava", JavaCompile::class.java)

            val kotlin = project.extensions["kotlin"] as KotlinJvmProjectExtension
            kotlin.jvmToolchain {
                languageVersion.set(buildModel.jvmToolchain.map { JavaLanguageVersion.of(it) })
            }

            val java = project.extensions["java"] as JavaPluginExtension
            java.withSourcesJar()

            configurations.named(java.sourceSets.getByName("main").implementationConfigurationName) {
                fromDependencyCollector(definition.dependencies.implementation)
            }
        }
    }
}

interface KotlinJvmLibraryDefinition : Definition<KotlinJvmLibraryBuildModel> {
    val jvmToolchain: Property<Int>

    @get:Nested
    val dependencies: AdventOfCodeDependencies
}

interface AdventOfCodeDependencies : Dependencies {
    val implementation: DependencyCollector
}

interface KotlinJvmLibraryBuildModel : BuildModel {
    val jvmToolchain: Provider<Int>
    val compileJavaTask: TaskProvider<JavaCompile>
}

internal abstract class DefaultKotlinJvmLibraryBuildModel : KotlinJvmLibraryBuildModel {
    override lateinit var jvmToolchain: Provider<Int>
    override lateinit var compileJavaTask: TaskProvider<JavaCompile>
}
