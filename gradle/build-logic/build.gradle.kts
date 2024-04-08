plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.plugins.kotlin.jvm.dep)
}


val Provider<PluginDependency>.dep: Provider<String> get() = map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }
