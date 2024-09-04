plugins {
    `kotlin-dsl`
    id("com.volkswagen.vocs.kfx.openapi") version "0.0.56"
}

dependencies {
    implementation(libs.plugins.kotlin.jvm.dep)
    compileOnly(libs.ktor.client.cio)
}

val Provider<PluginDependency>.dep: Provider<String> get() = map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }

tasks.convertOpenApiFiles {
    openapiFiles.from(files("central.json"))
}
