package com.github.ksoichiro.spelling

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

class FunctionalTest {
    static final String PLUGIN_ID = 'com.github.ksoichiro.spelling'

    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile
    List<File> pluginClasspath

    @Before
    void setup() {
        buildFile = testProjectDir.newFile("build.gradle")

        def pluginClasspathResource = getClass().classLoader.findResource("plugin-classpath.txt")
        if (pluginClasspathResource == null) {
            throw new IllegalStateException("Did not find plugin classpath resource, run `testClasses` build task.")
        }

        pluginClasspath = pluginClasspathResource.readLines()
            .collect { it.replace('\\', '\\\\') } // escape backslashes in Windows paths
            .collect { new File(it) }

        def srcDir = testProjectDir.newFolder("src", "main", "com", "example")
        new File(srcDir, "A.java").text = """
            |package com.example;
            |
            |public class A {
            |    // Foo
            |}""".stripMargin().stripIndent()
    }

    @Test
    public void basicInspectSpellingSuccess() throws IOException {
        def buildFileContent = """\
            |plugins {
            |    id '${PLUGIN_ID}'
            |}
            |spelling {
            |    excludes += 'build.gradle'
            |    definition {
            |        define forbidden: 'Blur', recommended: 'Bar'
            |    }
            |}""".stripMargin().stripIndent()
        buildFile.text = buildFileContent

        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("inspectSpelling")
            .withPluginClasspath(pluginClasspath)
            .build()

        assertEquals(result.task(":inspectSpelling").getOutcome(), TaskOutcome.SUCCESS)
    }

    @Test
    public void basicInspectSpellingFailure() throws IOException {
        def buildFileContent = """\
            |plugins {
            |    id '${PLUGIN_ID}'
            |}
            |spelling {
            |    excludes += 'build.gradle'
            |    definition {
            |        define forbidden: 'Foo', recommended: 'Bar'
            |    }
            |}""".stripMargin().stripIndent()
        buildFile.text = buildFileContent

        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("inspectSpelling")
            .withPluginClasspath(pluginClasspath)
            .buildAndFail()

        assertTrue(result.output.contains("Error: Found 'Foo', should replace to 'Bar'."))
        assertEquals(result.task(":inspectSpelling").getOutcome(), TaskOutcome.FAILED)
    }
}
