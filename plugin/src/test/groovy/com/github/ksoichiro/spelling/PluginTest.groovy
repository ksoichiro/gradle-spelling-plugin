package com.github.ksoichiro.spelling

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static org.junit.Assert.*

class PluginTest {
    private static final String PLUGIN_ID = 'com.github.ksoichiro.spelling'

    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile

    @Before
    void setup() {
        buildFile = testProjectDir.newFile("build.gradle")

        def srcDir = testProjectDir.newFolder("src", "main", "com", "example")
        new File(srcDir, "A.java").text = """\
            |package com.example;
            |
            |public class A {
            |    // Foo
            |}""".stripMargin().stripIndent()
    }

    @Test
    void apply() {
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: PLUGIN_ID

        assertTrue(project.tasks.inspectSpelling instanceof InspectSpellingTask)
        assertTrue(project.extensions.spelling instanceof SpellingExtension)
    }

    @Test
    public void inspect() {
        Project project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()
        project.apply plugin: PLUGIN_ID

        project.extensions.spelling.definition.with {
            define forbidden: 'Foo', recommended: 'Bar'
        }
        assertEquals(1, project.extensions.spelling.includes.size())
        assertEquals(1, project.extensions.spelling.excludes.size())
        assertEquals(1, project.extensions.spelling.definition.rules.size())

        try {
            project.evaluate()
            project.tasks.inspectSpelling.execute()
            fail()
        } catch (ignored) {
        }
    }

    @Test
    public void inspectWithExternalFile() {
        Project project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()
        project.apply plugin: PLUGIN_ID

        def configFile = testProjectDir.newFile('config.xml')
        project.extensions.spelling.externalConfigFile = configFile
        configFile.text = """\
            |<?xml version="1.0" ?>
            |<spelling>
            |    <message>Error: Found '%1s' but is should be '%2s'.</message>
            |    <excludes>
            |        <exclude>config.xml</exclude>
            |    </excludes>
            |    <definition>
            |        <rules>
            |            <rule forbidden="Foo" recommended="Bar" />
            |        </rules>
            |    </definition>
            |</spelling>""".stripMargin().stripIndent()
        try {
            project.evaluate()
            project.tasks.inspectSpelling.execute()
            fail()
        } catch (ignored) {
            assertEquals(1, project.extensions.spelling.includes.size())
            assertEquals(2, project.extensions.spelling.excludes.size())
            assertEquals(1, project.extensions.spelling.definition.rules.size())
        }
    }

    @Test
    public void configureRulesWithClosure() {
        Project project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()
        project.apply plugin: PLUGIN_ID

        project.extensions.spelling.definition.rules {
            define forbidden: 'Foo', recommended: 'Bar'
        }
        assertEquals(1, project.extensions.spelling.includes.size())
        assertEquals(1, project.extensions.spelling.excludes.size())
        assertEquals(1, project.extensions.spelling.definition.rules.size())

        try {
            project.evaluate()
            project.tasks.inspectSpelling.execute()
            fail()
        } catch (ignored) {
        }
    }

    @Test
    public void resetExcludesAndIncludes() {
        Project project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()
        project.apply plugin: PLUGIN_ID

        def configFile = testProjectDir.newFile('config.xml')
        project.extensions.spelling.externalConfigFile = configFile
        configFile.text = """\
            |<?xml version="1.0" ?>
            |<spelling>
            |    <excludes appendToDefault="false">
            |        <exclude>**/src/test/**/*</exclude>
            |    </excludes>
            |    <includes appendToDefault="false">
            |        <include>**/src/**/*</include>
            |    </includes>
            |    <definition>
            |        <rules>
            |            <rule forbidden="Foo" recommended="Bar" />
            |        </rules>
            |    </definition>
            |</spelling>""".stripMargin().stripIndent()
        try {
            project.evaluate()
            project.tasks.inspectSpelling.execute()
            fail()
        } catch (ignored) {
            assertEquals(1, project.extensions.spelling.includes.size())
            assertEquals("**/src/**/*", project.extensions.spelling.includes[0])
            assertEquals(1, project.extensions.spelling.excludes.size())
            assertEquals("**/src/test/**/*", project.extensions.spelling.excludes[0])
        }
    }

    @Test
    public void externalFileDoesNotExist() {
        Project project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()
        project.apply plugin: PLUGIN_ID

        project.extensions.spelling.externalConfigFile = new File(testProjectDir.root, "unknown")
        project.evaluate()
        project.tasks.inspectSpelling.execute()
        assertEquals(1, project.extensions.spelling.includes.size())
        assertEquals(1, project.extensions.spelling.excludes.size())
    }

    @Test
    public void overrideFailOnErrorToTrueWithXmlConfig() {
        Project project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()
        project.apply plugin: PLUGIN_ID

        def configFile = testProjectDir.newFile('config.xml')
        project.extensions.spelling.externalConfigFile = configFile
        configFile.text = """\
            |<?xml version="1.0" ?>
            |<spelling>
            |    <failOnError>true</failOnError>
            |    <definition>
            |        <rules>
            |            <rule forbidden="Foo" recommended="Bar" />
            |        </rules>
            |    </definition>
            |</spelling>""".stripMargin().stripIndent()
        try {
            project.evaluate()
            project.tasks.inspectSpelling.execute()
            fail()
        } catch (ignored) {
            assertTrue(project.extensions.spelling.failOnError)
        }
    }

    @Test
    public void overrideFailOnErrorToFalseWithXmlConfig() {
        Project project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()
        project.apply plugin: PLUGIN_ID

        def configFile = testProjectDir.newFile('config.xml')
        project.extensions.spelling.externalConfigFile = configFile
        configFile.text = """\
            |<?xml version="1.0" ?>
            |<spelling>
            |    <failOnError>false</failOnError>
            |    <definition>
            |        <rules>
            |            <rule forbidden="Foo" recommended="Bar" />
            |        </rules>
            |    </definition>
            |</spelling>""".stripMargin().stripIndent()
        project.evaluate()
        project.tasks.inspectSpelling.execute()
        assertFalse(project.extensions.spelling.failOnError)
    }

    @Test
    public void configureExtensionWithMissingMethodForDefinedProperty() {
        Project project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()
        project.apply plugin: PLUGIN_ID

        project.extensions.spelling.definition {
            define forbidden: 'Foo', recommended: 'Bar'
        }
    }

    @Test(expected = GradleException)
    public void configureExtensionThrowsExceptionWithMissingMethodForUndefinedProperty() {
        Project project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()
        project.apply plugin: PLUGIN_ID

        project.extensions.spelling.unknownProperty {
        }
    }
}
