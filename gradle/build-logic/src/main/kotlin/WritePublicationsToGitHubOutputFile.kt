import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.*

@CacheableTask
abstract class WritePublicationsToGitHubOutputFile : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val publicationFiles: ConfigurableFileCollection

    @get:OutputFile
    abstract val githubOutputFile: RegularFileProperty

    @TaskAction
    internal fun action() {
        githubOutputFile.get().asFile.writeText(
            publicationFiles.joinToString(prefix = "publishedFiles=", separator = ",") {
                it.readText()
            }
        )
    }
}
