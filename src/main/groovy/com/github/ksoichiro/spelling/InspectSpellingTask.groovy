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
        int violations = 0
        project.fileTree(dir: project.projectDir,
                excludes: ["**/build/**/*"],
                includes: ["**/*"]).each { File f ->
            f.eachLine { String line, Integer n ->
                extension.definition.rules.each { SpellingRule r ->
                    if (line.contains(r.forbidden)) {
                        violations++
                        println "${line}:${n}: Error: Found '${r.forbidden}, should replace to '${r.recommended}."
                    }
                }
            }
        }
        if (violations) {
            throw new GradleException("Violations found: ${violations} violations")
        }
    }
}