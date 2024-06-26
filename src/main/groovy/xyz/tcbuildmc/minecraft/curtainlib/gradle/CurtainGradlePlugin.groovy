package xyz.tcbuildmc.minecraft.curtainlib.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import xyz.tcbuildmc.minecraft.curtainlib.gradle.task.RunServerTask

class CurtainGradlePlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.pluginManager.apply JavaLibraryPlugin

        project.logger.lifecycle "CurtainGradle: ${CurtainGradlePlugin.class.package.implementationVersion}"
        project.logger.lifecycle "by TCBuildMC"
        def extension = project.extensions.create "curtainGradle", CurtainGradleExtension, project

        def bukkitLibrary = project.configurations.maybeCreate "bukkitLibrary"
        def bukkitMetadata = project.tasks.register("bukkitMetadata") {
            dependsOn project.tasks.named(JavaPlugin.PROCESS_RESOURCES_TASK_NAME).get()
        }

        def serverRuntimeClasspath = project.configurations.maybeCreate "serverRuntimeClasspath"
        def serverRuntimeMods = project.configurations.maybeCreate "serverRuntimeMods"
        def serverRuntimePlugins = project.configurations.maybeCreate "serverRuntimePlugins"
        def runServer = project.tasks.register "runServer", RunServerTask

        def runServerWithArtifact = project.tasks.register("runServerWithArtifact", RunServerTask) {
            dependsOn project.tasks.named(JavaPlugin.JAR_TASK_NAME).get()

            withArtifact = true
        }

        setupJavaVersion project, extension.languageVersion
    }

    private void setupJavaVersion(Project project, int version) {
        project.extensions.configure(JavaPluginExtension) {
            sourceCompatibility = version
            targetCompatibility = version

            toolchain {
                languageVersion.set JavaLanguageVersion.of(version)
            }
        }

        project.tasks.withType(JavaCompile).configureEach {
            options.release.set version
            options.encoding = "UTF-8"
        }

        project.tasks.withType(Test).configureEach {
            systemProperty "file.encoding", "UTF-8"

            ignoreFailures = true

            testLogging {
                exceptionFormat = "full"
                events "passed", "skipped", "failed"
            }
        }

        project.tasks.named(JavaPlugin.COMPILE_JAVA_TASK_NAME).get().dependsOn(project.tasks.named(BasePlugin.CLEAN_TASK_NAME))
    }
}
