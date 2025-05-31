plugins {
    id("setup")
}

dependencies {
    api(projects.year2023Day1)
}

tasks.compileJava {
    options.compilerArgumentProviders += object : CommandLineArgumentProvider {

        @InputFiles
        @PathSensitive(PathSensitivity.RELATIVE)
        val kotlinClasses = tasks.compileKotlin.flatMap { it.destinationDirectory }

        override fun asArguments(): List<String> = listOf(
            "--patch-module",
            "io.github.hfhbd.adventofcode.year2023.day2=${kotlinClasses.get().asFile.absolutePath}"
        )
    }
}
