package com.github.ksoichiro.spelling

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

class InspectSpellingTask extends DefaultTask {
    SpellingExtension extension

    InspectSpellingTask() {
        project.afterEvaluate {
            extension = project.extensions.spelling
        }
    }

    @TaskAction
    def exec() {
        extension.configure()
        int violations = 0
        project.fileTree(dir: project.projectDir,
                excludes: extension.excludes,
                includes: extension.includes).each { File f ->
            f.eachLine { String line, Integer n ->
                extension.definition.rules.each { SpellingRule r ->
                    if (line.contains(r.forbidden)) {
                        violations++
                        println "${f.absolutePath}:${n}: ${String.format(extension.message, r.forbidden, r.recommended)}"
                    }
                }
            }
        }
        if (violations && extension.failOnError) {
            throw new GradleException("Spelling inspection failed: ${violations} violations found")
        }
    }
}
