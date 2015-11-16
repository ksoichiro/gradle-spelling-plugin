package com.github.ksoichiro.spelling

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.util.ConfigureUtil

class SpellingExtension {
    Project project
    File externalConfigFile
    SpellingDefinition definition
    String message
    boolean failOnError
    List<String> excludes
    List<String> includes

    SpellingExtension(Project project) {
        this.project = project
        this.definition = new SpellingDefinition()
        this.message = "Error: Found '%1s', should replace to '%2s'.";
        this.failOnError = true
        this.excludes = ["**/build/**/*"]
        this.includes = ["**/*"]
    }

    /**
     * Configure this extension with external file if it exists.
     */
    void configure() {
        if (externalConfigFile) {
            if (externalConfigFile.exists()) {
                def rootNode = new XmlParser().parse(externalConfigFile)
                if (rootNode.definition) {
                    definition.configure(rootNode.definition as NodeList)
                }
                if (rootNode.message) {
                    message = rootNode.message.text()
                }
                if (rootNode.failOnError) {
                    failOnError = Boolean.valueOf(rootNode.failOnError.text() as String)
                }
                if (rootNode.excludes) {
                    if (rootNode.excludes[0].attribute('appendToDefault') != null
                        && !Boolean.valueOf(rootNode.excludes[0].@appendToDefault as String)) {
                        excludes = []
                    }
                    rootNode.excludes.exclude.each { exclude ->
                        excludes += exclude.text()
                    }
                }
                if (rootNode.includes) {
                    if (rootNode.includes.@appendToDefault != null
                        && !Boolean.valueOf(rootNode.includes.@appendToDefault as String)) {
                        includes = []
                    }
                    rootNode.includes.include.each { include ->
                        includes += include.text()
                    }
                }
            } else {
                println "Warning: configuration file not found: ${externalConfigFile.absolutePath}"
            }
        }
    }

    def methodMissing(String name, def args) {
        if (metaClass.hasProperty(this, name)) {
            return ConfigureUtil.configure(args[0] as Closure, this."$name")
        } else {
            throw new GradleException("Missing method: ${name}")
        }
    }
}
