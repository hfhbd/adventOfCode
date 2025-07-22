plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.plugins.kotlin.jvm.dep)
    implementation(libs.plugins.mavencentral.dep)
    compileOnly("io.ktor:ktor-client-java:3.2.1")
    compileOnly("io.ktor:ktor-client-logging:3.2.1")
}

val Provider<PluginDependency>.dep: Provider<String> get() = map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }

tasks.validatePlugins {
    enableStricterValidation.set(true)
}
