import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.ContentType.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSource
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.repositories.PasswordCredentials
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.credentials
import org.gradle.work.DisableCachingByDefault
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@DisableCachingByDefault
abstract class PublishToMavenCentral : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val uploadZip: RegularFileProperty

    @get:Input
    internal val credentials: Provider<PasswordCredentials> =
        project.providers.credentials(PasswordCredentials::class, "mavenCentral")

    @get:Inject
    internal abstract val workerExecutor: WorkerExecutor

    @get:InputFiles
    @get:Classpath
    internal abstract val workerClassPath: ConfigurableFileCollection

    @TaskAction
    fun publish() {
        workerExecutor.classLoaderIsolation {
            classpath.from(workerClassPath)
        }.submit(PublishWorker::class.java) {
            this.uploadZip.set(this@PublishToMavenCentral.uploadZip)
            this.userName.set(this@PublishToMavenCentral.credentials.map { it.username })
            this.password.set(this@PublishToMavenCentral.credentials.map { it.password })
        }
    }
}

abstract class PublishWorker : WorkAction<PublishWorker.PublishParameters> {
    interface PublishParameters : WorkParameters {
        val uploadZip: RegularFileProperty
        val userName: Property<String>
        val password: Property<String>
    }

    private val logger = org.gradle.api.logging.Logging.getLogger(PublishWorker::class.java)

    override fun execute(): Unit = runBlocking {
        val zipFile = parameters.uploadZip.asFile.get()
        val client = HttpClient(Java) {
            expectSuccess = true
            defaultRequest {
                url("https://central.sonatype.com")

                val userName = this@PublishWorker.parameters.userName.get()
                val password = this@PublishWorker.parameters.password.get()
                bearerAuth("$userName:$password".encodeBase64())
            }
            install(ContentNegotiation) {
                json()
            }
            install(Logging) {
                level = LogLevel.ALL
                logger = object : Logger {
                    override fun log(message: String) {
                        this@PublishWorker.logger.debug(message)
                    }
                }
            }
        }
        val deploymentId = client.submitFormWithBinaryData(
            url = "api/v1/publisher/upload",
            formData = formData {
                append(
                    key = "bundle",
                    filename = zipFile.name,
                    contentType = Application.OctetStream,
                    size = zipFile.length(),
                ) {
                    transferFrom(zipFile.inputStream().asSource())
                }
            },
        ) {
            parameter("publishingType", "AUTOMATIC")
        }.body<String>()
        while (true) {
            delay(500.milliseconds)
            val status = client.post("api/v1/publisher/status") {
                parameter("id", deploymentId)
            }.body<Status>()
            when (status.deployment.value.deploymentState) {
                DeploymentState.PENDING,
                DeploymentState.VALIDATING,
                DeploymentState.PUBLISHING,
                -> continue
                DeploymentState.PUBLISHED -> break
                DeploymentState.FAILED -> error(status.deployment.value.error)
            }
        }
    }
}

@Serializable
private data class Status(
    val deployment: Deployment,
)

@Serializable
private data class Deployment(
    val summary: String,
    val value: Value,
)

@Serializable
private data class Value(
    val deploymentId: String,
    val deploymentName: String,
    val deploymentState: DeploymentState,
    val purls: List<String>,
    val error: JsonObject,
)

@Serializable
private enum class DeploymentState {
    PENDING,
    VALIDATING,
    PUBLISHING,
    PUBLISHED,
    FAILED,
    ;
}