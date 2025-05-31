plugins {
    id("setup")
}

dependencies {
    api(projects.day2)
}

tasks.compileJava {
    options.compilerArgumentProviders += object : CommandLineArgumentProvider {

        @InputFiles
        @PathSensitive(PathSensitivity.RELATIVE)
        val kotlinClasses = tasks.compileKotlin.flatMap { it.destinationDirectory }

        override fun asArguments(): List<String> = listOf(
            "--patch-module",
            "io.github.hfhbd.adventofcode.day3=${kotlinClasses.get().asFile.absolutePath}"
        )
    }
}
