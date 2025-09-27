plugins {
    id("setup")
}

tasks.compileJava {
    options.compilerArgumentProviders += object : CommandLineArgumentProvider {

        @InputFiles
        @PathSensitive(PathSensitivity.RELATIVE)
        val kotlinClasses = tasks.compileKotlin.flatMap { it.destinationDirectory }

        override fun asArguments(): List<String> = listOf(
            "--patch-module",
            "io.github.hfhbd.adventofcode.year2023.day3=${kotlinClasses.get().asFile.absolutePath}"
        )
    }
}
