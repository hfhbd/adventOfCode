import dev.detekt.gradle.Detekt
import dev.detekt.gradle.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.dsl.Dependencies
import org.gradle.api.artifacts.dsl.DependencyCollector
import org.gradle.api.artifacts.repositories.PasswordCredentials
import org.gradle.api.attributes.Usage
import org.gradle.api.component.SoftwareComponentContainer
import org.gradle.features.annotations.BindsProjectType
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition
import org.gradle.features.binding.ProjectFeatureApplicationContext
import org.gradle.features.binding.ProjectTypeBinding
import org.gradle.features.binding.ProjectTypeBindingBuilder
import org.gradle.features.dsl.bindProjectType
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.plugins.PluginManager
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskContainer
import org.gradle.features.binding.ProjectTypeApplyAction
import org.gradle.features.file.ProjectFeatureLayout
import org.gradle.features.registration.ConfigurationRegistrar
import org.gradle.features.registration.TaskRegistrar
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.credentials
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.SigningExtension
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.dokka.gradle.tasks.DokkaGenerateTask
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import javax.inject.Inject

@BindsProjectType(AdventOfCodeProjectType::class)
abstract class AdventOfCodeProjectType : Plugin<Project>, ProjectTypeBinding {
    override fun apply(target: Project) {}
    override fun bind(builder: ProjectTypeBindingBuilder) {
        builder.bindProjectType("adventOfCode", ApplyAction::class)
            .withUnsafeDefinition()
            .withUnsafeApplyAction()
    }

    internal abstract class ApplyAction : ProjectTypeApplyAction<AdventOfCodeDefinition, BuildModel.None> {
        @get:Inject
        abstract val pluginManager: PluginManager

        @get:Inject
        abstract val taskRegistrar: TaskRegistrar

        @get:Inject
        abstract val configurationRegistrar: ConfigurationRegistrar

        @get:Inject
        abstract val configurations: ConfigurationContainer

        @get:Inject
        abstract val tasks: TaskContainer

        @get:Inject
        abstract val components: SoftwareComponentContainer

        @get:Inject
        abstract val providers: ProviderFactory

        @get:Inject
        abstract val layout: ProjectFeatureLayout

        @get:Inject
        abstract val project: Project

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: AdventOfCodeDefinition,
            buildModel: BuildModel.None
        ) {
            pluginManager.apply("org.jetbrains.kotlin.jvm")
            pluginManager.apply("java-test-fixtures")
            pluginManager.apply("maven-publish")
            pluginManager.apply("signing")
            pluginManager.apply("io.github.hfhbd.mavencentral")
            pluginManager.apply("dev.detekt")
            pluginManager.apply("dev.sigstore.sign")
            pluginManager.apply("org.jetbrains.dokka")
            pluginManager.apply("org.jetbrains.dokka-javadoc")

            val kotlin = project.extensions["kotlin"] as KotlinJvmProjectExtension
            kotlin.jvmToolchain(21)

            val java = project.extensions["java"] as JavaPluginExtension
            java.withSourcesJar()

            configurations.named(java.sourceSets.getByName("main").implementationConfigurationName) {
                fromDependencyCollector(definition.dependencies.implementation)
            }

            val publishing = project.extensions[PublishingExtension.NAME] as PublishingExtension
            val mavenPublication = publishing.publications.register<MavenPublication>("gpr") {
                from(components["java"])
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
                    providers.gradleProperty("ignoreDetektFailures")
                        .map { it.toBoolean() }
                        .orElse(false)
                )
            }

            taskRegistrar.register("deleteDetektBaseline", Delete::class.java) {
                delete(project.tasks.named("detekt", Detekt::class).flatMap { it.baseline })
            }

            configurationRegistrar.consumable("sarif") {
                attributes {
                    attribute(Usage.USAGE_ATTRIBUTE, named("detekt-sarif"))
                }
                outgoing {
                    artifact(
                        tasks.named("detekt", Detekt::class).flatMap { it.reports.sarif.outputLocation }
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
                        localDirectory.set(layout.projectDirectory.dir("src/main/kotlin"))
                        remoteUrl.set(project.uri("https://github.com/hfhbd/adventOfCode/tree/main/$module/src/main/kotlin"))
                        remoteLineSuffix.set("#L")
                    }
                    samples.from(layout.projectDirectory.file("src/test/kotlin"))
                }
            }

            // To generate documentation in HTML
            taskRegistrar.register("dokkaHtmlJar", Jar::class.java) {
                description = "A HTML Documentation JAR containing Dokka HTML"
                from(
                    project.tasks.named("dokkaGeneratePublicationHtml", DokkaGenerateTask::class)
                        .flatMap { it.outputDirectory }
                )
                archiveClassifier.set("html-doc")
            }

            // To generate documentation in Javadoc
            val dokkaJavadocJar = taskRegistrar.register("dokkaJavadocJar", Jar::class.java) {
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
    val dependencies: AdventOfCodeDependencies
}

interface AdventOfCodeDependencies : Dependencies {
    val implementation: DependencyCollector
}
