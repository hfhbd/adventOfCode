import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.dsl.DependencyCollector
import org.gradle.api.plugins.jvm.JvmComponentDependencies

interface JvmLibraryDependencies : JvmComponentDependencies {
    /**
     * Returns a [org.gradle.api.artifacts.dsl.DependencyCollector] that collects the set of api dependencies.
     *
     *
     * `api` dependencies are used at compilation and runtime.
     *
     * @return a [org.gradle.api.artifacts.dsl.DependencyCollector] that collects the set of api dependencies
     * @since 9.4.0
     */
    val api: DependencyCollector

    /**
     * Returns a [org.gradle.api.artifacts.dsl.DependencyCollector] that collects the set of compile-only api dependencies.
     *
     *
     * `compileOnlyApi` dependencies are used only at compilation and are not available at runtime.
     *
     * @return a [org.gradle.api.artifacts.dsl.DependencyCollector] that collects the set of compile-only api dependencies
     * @since 9.4.0
     */
    val compileOnlyApi: DependencyCollector
}

fun ConfigurationContainer.wire(prefix: String, dependencies: JvmLibraryDependencies) {
    named("${prefix}Api".replaceFirstChar { it.lowercase() }) {
        fromDependencyCollector(dependencies.api)
    }
    named("${prefix}CompileOnlyApi".replaceFirstChar { it.lowercase() }) {
        fromDependencyCollector(dependencies.compileOnlyApi)
    }
    wire(prefix, dependencies as JvmComponentDependencies)
}

fun ConfigurationContainer.wire(prefix: String, dependencies: JvmComponentDependencies) {
    named("${prefix}Implementation".replaceFirstChar { it.lowercase() }) {
        fromDependencyCollector(dependencies.implementation)
    }
    named("${prefix}CompileOnly".replaceFirstChar { it.lowercase() }) {
        fromDependencyCollector(dependencies.compileOnly)
    }
    named("${prefix}RuntimeOnly".replaceFirstChar { it.lowercase() }) {
        fromDependencyCollector(dependencies.runtimeOnly)
    }
    named("${prefix}AnnotationProcessor".replaceFirstChar { it.lowercase() }) {
        fromDependencyCollector(dependencies.annotationProcessor)
    }
}
