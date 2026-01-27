plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.plugins.kotlin.jvm.dep)
    implementation(libs.plugins.mavencentral.dep)
    implementation(libs.plugins.detekt.dep)
    implementation(libs.plugins.foojay.dep)
    implementation(libs.plugins.sigstore.dep)
    implementation(libs.plugins.dokka.dep)
}

gradlePlugin.plugins.register("myRepos") {
    id = "myRepos"
    implementationClass = "MyReposPlugin"
}

val Provider<PluginDependency>.dep: Provider<String> get() = map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }

tasks.validatePlugins {
    enableStricterValidation.set(true)
}

interface Foo : Named
extensions.add("foo", objects.domainObjectContainer(Foo::class))
