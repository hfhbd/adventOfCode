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
            "io.github.hfhbd.adventofcode.day1=${kotlinClasses.get().asFile.absolutePath}"
        )
    }
}
