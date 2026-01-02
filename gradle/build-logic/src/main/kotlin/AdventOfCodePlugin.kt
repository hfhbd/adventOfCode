import dev.detekt.gradle.Detekt
import dev.detekt.gradle.extensions.DetektExtension
import org.gradle.api.DomainObjectCollection
import org.gradle.api.Incubating
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyCollector
import org.gradle.api.artifacts.dsl.GradleDependencies
import org.gradle.api.artifacts.repositories.PasswordCredentials
import org.gradle.api.attributes.Usage
import org.gradle.api.internal.plugins.BindsProjectType
import org.gradle.api.internal.plugins.BuildModel
import org.gradle.api.internal.plugins.DeclaredProjectFeatureBindingBuilder
import org.gradle.api.internal.plugins.Definition
import org.gradle.api.internal.plugins.ProjectFeatureApplicationContext
import org.gradle.api.internal.plugins.ProjectTypeBinding
import org.gradle.api.internal.plugins.ProjectTypeBindingBuilder
import org.gradle.api.internal.plugins.features.dsl.bindProjectType
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.api.plugins.jvm.JvmTestSuiteTarget
import org.gradle.api.plugins.jvm.PlatformDependencyModifiers
import org.gradle.api.provider.Provider
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.Nested
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.credentials
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.registering
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.SigningExtension
import org.gradle.testing.base.TestingExtension
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.dokka.gradle.tasks.DokkaGenerateTask
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import kotlin.collections.toList

@BindsProjectType(AdventOfCodePlugin::class)
abstract class AdventOfCodePlugin : Plugin<Project>, ProjectTypeBinding {
    override fun apply(target: Project) {}
    override fun bind(builder: ProjectTypeBindingBuilder) {
        builder.bindProjectType("adventOfCode") { definition: AdventOfCodeDefinition ->
            project.pluginManager.apply("org.jetbrains.kotlin.jvm")
            project.pluginManager.apply("java-test-fixtures")
            project.pluginManager.apply("jvm-test-suite")
            project.pluginManager.apply("maven-publish")
            project.pluginManager.apply("signing")
            project.pluginManager.apply("io.github.hfhbd.mavencentral")
            project.pluginManager.apply("dev.detekt")
            project.pluginManager.apply("dev.sigstore.sign")
            project.pluginManager.apply("org.jetbrains.dokka")
            project.pluginManager.apply("org.jetbrains.dokka-javadoc")

            val kotlin = project.extensions["kotlin"] as KotlinJvmProjectExtension
            kotlin.jvmToolchain(21)

            val testing = project.extensions["testing"] as TestingExtension
            testing.suites.withType<JvmTestSuite>().all {
                val jvmTestSuite = this
                jvmTestSuite.useKotlinTest()

                // use register instead to let Gradle manage the instance
                definition.testing.suites.add(object : JvmDclTestSuite {
                    override fun getTargets() = jvmTestSuite.targets as NamedDomainObjectContainer<JvmTestSuiteTarget>
                    override val dependencies = object : JvmDclComponentDependencies {
                        override val implementation = jvmTestSuite.dependencies.implementation
                        override val compileOnly = jvmTestSuite.dependencies.compileOnly
                        override val runtimeOnly = jvmTestSuite.dependencies.runtimeOnly
                        override val annotationProcessor = jvmTestSuite.dependencies.annotationProcessor

                        override fun getPlatform() = jvmTestSuite.dependencies.platform
                        override fun getEnforcedPlatform() = jvmTestSuite.dependencies.enforcedPlatform

                        override fun getDependencyFactory() = jvmTestSuite.dependencies.dependencyFactory

                        override fun getDependencyConstraintFactory() =
                            jvmTestSuite.dependencies.dependencyConstraintFactory

                        override fun getProject(): Project = jvmTestSuite.dependencies.project
                        override fun getObjectFactory() = jvmTestSuite.dependencies.objectFactory
                    }

                    override fun getName(): String = jvmTestSuite.name
                })
            }

            val java = project.extensions["java"] as JavaPluginExtension
            java.withSourcesJar()

            val publishing = project.extensions[PublishingExtension.NAME] as PublishingExtension
            val mavenPublication = publishing.publications.register<MavenPublication>("gpr") {
                from(project.components["java"])
            }
            publishing.apply {
                repositories {
                    maven(url = "https://maven.pkg.github.com/hfhbd/adventOfCode") {
                        name = "GitHubPackages"
                        credentials(PasswordCredentials::class)
                    }
                    maven(url = "https://central.sonatype.com/repository/maven-snapshots/") {
                        name = "mavenCentralSnapshot"
                        credentials(PasswordCredentials::class)
                    }
                }
                publications.withType<MavenPublication>().configureEach {
                    pom {
                        name.set("hfhbd AdventOfCode")
                        description.set("hfhbd AdventOfCode")
                        url.set("https://github.com/hfhbd/adventOfCode")
                        licenses {
                            license {
                                name.set("Apache-2.0")
                                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                            }
                        }
                        developers {
                            developer {
                                id.set("hfhbd")
                                name.set("Philip Wedemann")
                                email.set("mybztg+mavencentral@icloud.com")
                            }
                        }
                        scm {
                            connection.set("scm:git://github.com/hfhbd/adventOfCode.git")
                            developerConnection.set("scm:git://github.com/hfhbd/adventOfCode.git")
                            url.set("https://github.com/hfhbd/adventOfCode")
                        }

                        distributionManagement {
                            repository {
                                id.set("github")
                                name.set("GitHub hfhbd Apache Maven Packages")
                                url.set("https://maven.pkg.github.com/hfhbd/adventOfCode")
                            }
                        }
                    }
                }
            }

            val signing = project.extensions.getByName("signing") as SigningExtension
            signing.apply {
                useInMemoryPgpKeys(
                    project.providers.gradleProperty("signingKey").orNull,
                    project.providers.gradleProperty("signingPassword").orNull,
                )
                isRequired = project.providers.gradleProperty("signingKey").isPresent
                sign(publishing.publications)
            }

            val detekt = project.extensions["detekt"] as DetektExtension
            detekt.apply {
                parallel.set(true)
                autoCorrect.set(true)
                buildUponDefaultConfig.set(true)
                ignoreFailures.set(
                    project.providers.gradleProperty("ignoreDetektFailures")
                        .map { it.toBoolean() }
                        .orElse(false)
                )
            }

            project.tasks.register<Delete>("deleteDetektBaseline") {
                delete(project.tasks.named("detekt", Detekt::class).flatMap { it.baseline })
            }

            project.configurations.consumable("sarif") {
                attributes {
                    attribute(Usage.USAGE_ATTRIBUTE, named("detekt-sarif"))
                }
                outgoing {
                    artifact(
                        project.tasks.named("detekt", Detekt::class).flatMap { it.reports.sarif.outputLocation }
                    )
                }
            }

            val dokka = project.extensions.getByName("dokka") as DokkaExtension
            dokka.apply {
                val module = project.name
                dokkaSourceSets.named("main") {
                    reportUndocumented.set(true)
                    includes.from("README.md")
                    sourceLink {
                        localDirectory.set(project.file("src/main/kotlin"))
                        remoteUrl.set(project.uri("https://github.com/hfhbd/adventOfCode/tree/main/$module/src/main/kotlin"))
                        remoteLineSuffix.set("#L")
                    }
                    samples.from(project.file("src/test/kotlin"))
                }
            }

            // To generate documentation in HTML
            val dokkaHtmlJar by project.tasks.registering(Jar::class) {
                description = "A HTML Documentation JAR containing Dokka HTML"
                from(
                    project.tasks.named("dokkaGeneratePublicationHtml", DokkaGenerateTask::class)
                        .flatMap { it.outputDirectory }
                )
                archiveClassifier.set("html-doc")
            }

            // To generate documentation in Javadoc
            val dokkaJavadocJar by project.tasks.registering(Jar::class) {
                description = "A Javadoc JAR containing Dokka Javadoc"
                from(
                    project.tasks.named("dokkaGeneratePublicationJavadoc", DokkaGenerateTask::class)
                        .flatMap { it.outputDirectory }
                )
                archiveClassifier.set("javadoc")
            }

            mavenPublication {
                artifact(dokkaJavadocJar)
            }
        }
    }
}

interface AdventOfCodeDefinition : Definition<BuildModel.None> {
    @get:Nested
    val testing: DclTestingExtension
}

interface DclTestingExtension {
    @get:Nested
    val suites: NamedDomainObjectContainer<JvmDclTestSuite>
}

interface JvmDclTestSuite : Named {
    // https://github.com/gradle/gradle/issues/36176
    // fun useKotlinTest()

    fun getTargets(): NamedDomainObjectContainer<JvmTestSuiteTarget>

    @get:Nested
    val dependencies: JvmDclComponentDependencies
}

// https://github.com/gradle/gradle/issues/36173
@Incubating
interface JvmDclComponentDependencies : PlatformDependencyModifiers, GradleDependencies {
    val implementation: DependencyCollector
    val compileOnly: DependencyCollector
    val runtimeOnly: DependencyCollector
    val annotationProcessor: DependencyCollector
}

// https://github.com/gradle/gradle/issues/35870
public inline fun <reified OwnDefinition : Definition<BuildModel.None>> ProjectTypeBindingBuilder.bindProjectType(
    name: String,
    noinline block: ProjectFeatureApplicationContext.(OwnDefinition) -> Unit,
): DeclaredProjectFeatureBindingBuilder<OwnDefinition, BuildModel.None> =
    bindProjectType(name) { definition: OwnDefinition, _: BuildModel.None -> block(definition) }

inline fun <reified T : Any> DomainObjectCollection<T>.getElements(project: Project): Provider<out Collection<T>> = project.provider {
    toList()
}
