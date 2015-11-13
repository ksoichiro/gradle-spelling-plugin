package com.github.ksoichiro.spelling

import org.gradle.api.Plugin
import org.gradle.api.Project

class SpellingPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.extensions.create("spelling", SpellingExtension, project)
        project.task("inspectSpelling", type: InspectSpellingTask)
    }
}
