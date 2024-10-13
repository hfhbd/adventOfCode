import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

@CacheableTask
abstract class SharePublications : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.ABSOLUTE)
    abstract val sourceFiles: ConfigurableFileCollection

    @get:OutputFile
    abstract val targetFile: RegularFileProperty

    @TaskAction
    internal fun action() {
        targetFile.get().asFile.writeText(
            sourceFiles.joinToString(",") { it.absolutePath }
        )
    }
}
