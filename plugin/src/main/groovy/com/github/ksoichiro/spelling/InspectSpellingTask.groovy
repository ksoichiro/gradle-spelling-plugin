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
            File temporary = createTemporaryFile(f)
            PrintWriter writer
            boolean fixed = false
            if (extension.fixAutomatically) {
                temporary.createNewFile()
                writer = temporary.newPrintWriter()
            }
            f.eachLine { String line, Integer n ->
                String replaced = line
                extension.definition.rules.each { SpellingRule r ->
                    if (line.contains(r.forbidden)) {
                        if (extension.fixAutomatically) {
                            fixed = true
                            replaced = replaced.replace(r.forbidden, r.recommended)
                        } else {
                            violations++
                            println "${f.absolutePath}:${n}: ${String.format(extension.message, r.forbidden, r.recommended)}"
                        }
                    }
                }
                if (extension.fixAutomatically) {
                    writer.println replaced
                }
            }
            if (fixed) {
                writer.close()
                f.text = temporary.text
            }
            temporary.delete()
        }
        if (violations && extension.failOnError && !extension.fixAutomatically) {
            throw new GradleException("Spelling inspection failed: ${violations} violations found")
        }
    }

    static File createTemporaryFile(File from) {
        new File(from.parentFile, ".${from.name}.tmp.${System.currentTimeMillis()}")
    }
}
