import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.streams.*
import kotlinx.coroutines.*
import org.gradle.api.*
import org.gradle.api.artifacts.repositories.*
import org.gradle.api.file.*
import org.gradle.api.provider.*
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.*
import org.gradle.work.*
import org.gradle.workers.*
import javax.inject.*

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

    @get:Classpath
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
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

    override fun execute(): Unit = runBlocking {
        val zipFile = parameters.uploadZip.asFile.get()
        val deploymentID = HttpClient(CIO).submitFormWithBinaryData(
            url = "https://central.sonatype.com/api/v1/publisher/upload",
            formData = formData {
                append(
                    key = "bundle",
                    filename = zipFile.name,
                    contentType = ContentType.Application.OctetStream,
                    size = zipFile.length(),
                ) {
                   zipFile.inputStream().copyTo(outputStream())
                }
            },
        ) {
            val userName = parameters.userName.get()
            val password = parameters.password.get()
            bearerAuth("$userName:$password".encodeBase64())
        }.bodyAsText()


    }
}
