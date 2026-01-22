import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.plugins.BindsProjectType
import org.gradle.api.internal.plugins.BuildModel
import org.gradle.api.internal.plugins.Definition
import org.gradle.api.internal.plugins.ProjectTypeBinding
import org.gradle.api.internal.plugins.ProjectTypeBindingBuilder

@BindsProjectType(FooProjectType::class)
abstract class FooProjectType : Plugin<Project>, ProjectTypeBinding {
    override fun apply(target: Project) {}
    override fun bind(builder: ProjectTypeBindingBuilder) {
        builder.bindProjectType("foo") { _: Foo -> }
    }
}

interface Foo : Definition<BuildModel.None>
