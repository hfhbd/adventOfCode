import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.credentials.PasswordCredentials
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.credentials
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Remote operation")
abstract class UploadSignatures : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val signatures: ConfigurableFileCollection

    @get:Input
    abstract val githubApiUrl: Property<String>

    @get:Input
    val githubCredentials = project.providers.credentials(PasswordCredentials::class, "GitHubPackages")

    @get:Input
    abstract val githubRepository: Property<String>


    @TaskAction
    internal fun uploadSignatures(): Unit = runBlocking {
        HttpClient(CIO) {
            defaultRequest {
                url.takeFrom(githubApiUrl.get())
                accept(ContentType.parse("application/vnd.github+json"))
                bearerAuth(githubCredentials.get().password!!)
            }
            expectSuccess = true
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        this@UploadSignatures.logger.info(message)
                    }
                }
                level = LogLevel.ALL
            }
        }.use { client ->
            for (file in signatures) {
                client.post(
                    "/repos/${githubRepository.get()}/attestations"
                ) {
                    //language=json
                    val bundle = """
                        { "bundle": ${file.readText()} }
                    """.trimIndent()
                    setBody(bundle)
                }
            }
        }
    }
}
