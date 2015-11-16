package com.github.ksoichiro.spelling

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

import static org.junit.Assert.assertTrue

class PluginTest {
    private static final String PLUGIN_ID = 'com.github.ksoichiro.spelling'

    @Test
    void apply() {
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: PLUGIN_ID

        assertTrue(project.tasks.inspectSpelling instanceof InspectSpellingTask)
        assertTrue(project.extensions.spelling instanceof SpellingExtension)
    }
}
