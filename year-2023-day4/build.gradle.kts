plugins {
    id("setup")
    id("app.softwork.kobol")
}

kobol {
    dependencies {
        compiler(kotlin())
        compiler(kotlinFileJava())

        compiler(pluginBooleanexpressions())
        compiler(pluginConstvariables())
        compiler(pluginInlining())
        compiler(pluginNosynthetic())
        compiler(pluginObjects())

        compiler("app.cash.sql-psi:environment:0.5.2")
        compiler("app.cash.sqldelight:compiler-env:2.1.0")
    }
}

configurations.kobolCompilerClasspath {
    exclude("app.softwork.sql.psi", "core")
}

tasks.compileCobolMain {
    outputFolder.set(layout.buildDirectory.dir("generated/kobol/main"))
}
tasks.compileCobolTest {
    outputFolder.set(layout.buildDirectory.dir("generated/kobol/test"))
}
tasks.compileCobolTestFixtures {
    outputFolder.set(layout.buildDirectory.dir("generated/kobol/test-fixtures"))
}
