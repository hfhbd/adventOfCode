import org.gradle.api.Action
import org.gradle.api.Incubating
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.dsl.DependencyCollector
import org.gradle.api.artifacts.dsl.GradleDependencies
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.internal.plugins.BindsProjectFeature
import org.gradle.api.internal.plugins.BuildModel
import org.gradle.api.internal.plugins.Definition
import org.gradle.api.internal.plugins.ProjectFeatureBinding
import org.gradle.api.internal.plugins.ProjectFeatureBindingBuilder
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.api.plugins.jvm.JvmTestSuiteTarget
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import org.gradle.declarative.dsl.model.annotations.HiddenInDefinition
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.testing.base.TestSuiteTarget
import org.gradle.testing.base.TestingExtension

@BindsProjectFeature(JvmTestSuiteFeature::class)
abstract class JvmTestSuiteFeature : Plugin<Project>, ProjectFeatureBinding {
    override fun apply(target: Project) {}
    override fun bind(builder: ProjectFeatureBindingBuilder) {
        builder.bindProjectFeature("testing") { definition: DclTestingExtension, _: AdventOfCodeDefinition ->
            project.pluginManager.apply("jvm-test-suite")

            val testing = project.extensions["testing"] as TestingExtension
            testing.suites.withType<JvmTestSuite>().all {
                // https://github.com/gradle/gradle/issues/36176
                useKotlinTest()
            }

            definition.getSuites().all {
                val dclJvmSuite = this
                val action: Action<JvmTestSuite> = Action {
                    dependencies.implementation.bundle(dclJvmSuite.dependencies.implementation.dependencies)
                    dependencies.compileOnly.bundle(dclJvmSuite.dependencies.compileOnly.dependencies)
                    dependencies.runtimeOnly.bundle(dclJvmSuite.dependencies.runtimeOnly.dependencies)
                    dependencies.annotationProcessor.bundle(dclJvmSuite.dependencies.annotationProcessor.dependencies)

                    dclJvmSuite.getTargets().all {
                        val dclTestSuiteTarget = this
                        val action: Action<JvmTestSuiteTarget> = Action {
                            project.tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME) {
                                // TaskProvider<Test> getTestTask(); is not supported in DCL, so we use this workaround for lifecycle task dependencies
                                val s = dclTestSuiteTarget.testing.dependsOnCheck.flatMap {
                                    if (it) {
                                        testTask
                                    } else {
                                        project.provider { emptyList<Task>() }
                                    }
                                }.orElse(emptyList<Task>())
                                dependsOn(s)
                            }

                            testTask {
                                // JavaForkOptions uses Any/Object, that is not supported in DCL
                                // No provider api migration (yet), so there is no provider support, thus calling get
                                environment(dclTestSuiteTarget.testing.javaForkOptions.environment.get())
                            }
                        }
                        if (name == dclJvmSuite.name) {
                            targets.named(name, action)
                        } else {
                            targets.register(name, action)
                        }
                    }
                }
                if (dclJvmSuite.name == "test") {
                    testing.suites.named(dclJvmSuite.name, JvmTestSuite::class, action)
                } else {
                    testing.suites.register(dclJvmSuite.name, JvmTestSuite::class, action)
                }
            }
        }
    }
}

// Can't reuse TestingExtension from core-api because of DomainObjectCollection<? extends TestSuiteTarget> getTargets();
// OUT/? extends is not (yet?) supported in DCL
interface DclTestingExtension : Definition<BuildModel.None> {
    @Nested
    fun getSuites(): NamedDomainObjectContainer<JvmDclTestSuite>
}

// Can't extend TestSuite from core-api because of DomainObjectCollection<? extends TestSuiteTarget> getTargets();
// OUT/? extends is not (yet?) supported in DCL
interface JvmDclTestSuite : Named {
    // https://github.com/gradle/gradle/issues/36176
    // fun useKotlinTest()

    fun getTargets(): NamedDomainObjectContainer<JvmDclTestSuiteTarget>

    @get:Nested
    val dependencies: JvmDclComponentDependencies

    @HiddenInDefinition
    fun dependencies(action: Action<JvmDclComponentDependencies>) {
        action.execute(dependencies)
    }
}

interface JvmDclTestSuiteTarget : TestSuiteTarget, Named {
    // TaskProvider<Test> getTestTask(); is not supported in DCL
    @get:Nested
    val testing: TestingSpec

    @HiddenInDefinition
    fun testing(action: Action<TestingSpec>) {
        action.execute(testing)
    }

    override fun getBinaryResultsDirectory(): DirectoryProperty
}

interface TestingSpec {
    // TaskProvider<Test> getTestTask(); is not supported in DCL, so we use this workaround for lifecycle task dependencies
    val dependsOnCheck: Property<Boolean>

    // JavaForkOptions uses Any/Object, that is not supported in DCL
    @get:Nested
    val javaForkOptions: JavaDclForkOptions

    @HiddenInDefinition
    fun javaForkOptions(action: Action<JavaDclForkOptions>) {
        action.execute(javaForkOptions)
    }
}

interface JavaDclForkOptions {
    val environment: MapProperty<String, String>
}

// https://github.com/gradle/gradle/issues/36173
@Incubating
interface JvmDclComponentDependencies : GradleDependencies {
    val implementation: DependencyCollector
    val compileOnly: DependencyCollector
    val runtimeOnly: DependencyCollector
    val annotationProcessor: DependencyCollector
}
