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

    SpellingExtension(Project project) {
        this.project = project
        this.definition = new SpellingDefinition()
        this.message = "Error: Found '%1s', should replace to '%2s'.";
        this.failOnError = true
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
