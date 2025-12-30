import dev.detekt.gradle.Detekt
import dev.detekt.gradle.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.PasswordCredentials
import org.gradle.api.attributes.Usage
import org.gradle.api.internal.plugins.BindsProjectType
import org.gradle.api.internal.plugins.BuildModel
import org.gradle.api.internal.plugins.Definition
import org.gradle.api.internal.plugins.ProjectTypeBinding
import org.gradle.api.internal.plugins.ProjectTypeBindingBuilder
import org.gradle.api.internal.plugins.features.dsl.bindProjectType
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.Delete
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.credentials
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.registering
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.SigningExtension
import org.gradle.testing.base.TestingExtension
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.dokka.gradle.tasks.DokkaGenerateTask
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

@BindsProjectType(AdventOfCodePlugin.Binding::class)
abstract class AdventOfCodePlugin : Plugin<Project> {
    override fun apply(project: Project) {}
    class Binding : ProjectTypeBinding {
        override fun bind(builder: ProjectTypeBindingBuilder) {
            builder.bindProjectType("adventOfCode") { _: AdventOfCodeDefinition, _: BuildModel.None ->
                project.pluginManager.apply("org.jetbrains.kotlin.jvm")
                project.pluginManager.apply("java-test-fixtures")
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
                testing.suites.withType(JvmTestSuite::class).configureEach {
                    useKotlinTest()
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
                    ignoreFailures.set(project.providers.gradleProperty("ignoreDetektFailures").map { it.toBoolean() }
                        .orElse(false))
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
                            project.tasks.named("detekt", Detekt::class).flatMap { it.reports.sarif.outputLocation })
                    }
                }

                val dokka = project.extensions.getByName("dokka") as DokkaExtension
                dokka.apply {
                    val module = project.name
                    dokkaSourceSets.configureEach {
                        reportUndocumented.set(true)
                        includes.from("README.md")
                        val sourceSetName = name
                        project.file("$module/src/$sourceSetName").takeIf { it.exists() }?.let {
                            sourceLink {
                                localDirectory.set(project.file("src/$sourceSetName/kotlin"))
                                remoteUrl.set(project.uri("https://github.com/hfhbd/adventOfCode/tree/main/$module/src/$sourceSetName/kotlin"))
                                remoteLineSuffix.set("#L")
                            }
                        }
                    }
                }

                // To generate documentation in HTML
                val dokkaHtmlJar by project.tasks.registering(Jar::class) {
                    description = "A HTML Documentation JAR containing Dokka HTML"
                    from(
                        project.tasks.named("dokkaGeneratePublicationHtml", DokkaGenerateTask::class)
                            .flatMap { it.outputDirectory })
                    archiveClassifier.set("html-doc")
                }

                // To generate documentation in Javadoc
                val dokkaJavadocJar by project.tasks.registering(Jar::class) {
                    description = "A Javadoc JAR containing Dokka Javadoc"
                    from(
                        project.tasks.named("dokkaGeneratePublicationJavadoc", DokkaGenerateTask::class)
                            .flatMap { it.outputDirectory })
                    archiveClassifier.set("javadoc")
                }

                mavenPublication {
                    artifact(dokkaJavadocJar)
                }
            }
        }
    }
}

interface AdventOfCodeDefinition : Definition<BuildModel.None>
