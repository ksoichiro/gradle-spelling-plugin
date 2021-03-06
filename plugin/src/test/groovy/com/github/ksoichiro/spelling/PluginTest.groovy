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
    public void overrideFixAutomaticallyToTrueWithXmlConfig() {
        Project project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()
        project.apply plugin: PLUGIN_ID

        def configFile = testProjectDir.newFile('config.xml')
        project.extensions.spelling.externalConfigFile = configFile
        configFile.text = """\
            |<?xml version="1.0" ?>
            |<spelling>
            |    <fixAutomatically>true</fixAutomatically>
            |    <definition>
            |        <rules>
            |            <rule forbidden="Foo" recommended="Bar" />
            |        </rules>
            |    </definition>
            |</spelling>""".stripMargin().stripIndent()
        project.evaluate()
        project.tasks.inspectSpelling.execute()
        assertEquals("""package com.example;
            |
            |public class A {
            |    // Bar
            |}
            |""".stripMargin().stripIndent(), new File("${testProjectDir.root}/src/main/com/example/A.java").text.replaceAll("\r", ""))
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

    @Test
    public void configureExtensionNotThrowExceptionWithoutAnyChildrenTags() {
        Project project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()
        project.apply plugin: PLUGIN_ID

        def configFile = testProjectDir.newFile('config.xml')
        project.extensions.spelling.externalConfigFile = configFile
        configFile.text = """\
            |<?xml version="1.0" ?>
            |<spelling>
            |</spelling>""".stripMargin().stripIndent()
        project.evaluate()
        project.tasks.inspectSpelling.execute()
    }

    @Test
    public void configureExtensionNotThrowExceptionWithoutAnyRuleTags() {
        Project project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()
        project.apply plugin: PLUGIN_ID

        def configFile = testProjectDir.newFile('config.xml')
        project.extensions.spelling.externalConfigFile = configFile
        configFile.text = """\
            |<?xml version="1.0" ?>
            |<spelling>
            |    <definition>
            |    </definition>
            |</spelling>""".stripMargin().stripIndent()
        project.evaluate()
        project.tasks.inspectSpelling.execute()
    }

    @Test
    public void configureExtensionWithExcludesTagNotAppendToDefault() {
        Project project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()
        project.apply plugin: PLUGIN_ID

        def configFile = testProjectDir.newFile('config.xml')
        project.extensions.spelling.externalConfigFile = configFile
        configFile.text = """\
            |<?xml version="1.0" ?>
            |<spelling>
            |    <excludes appendToDefault="false">
            |        <exclude pattern=".gradle/**/*" />
            |        <exclude pattern=".idea/**/*" />
            |    </excludes>
            |</spelling>""".stripMargin().stripIndent()
        project.evaluate()
        project.tasks.inspectSpelling.execute()
        assertEquals(2, project.extensions.spelling.excludes.size())
        assertFalse(project.extensions.spelling.excludes.any { it.equals("**/build/**/*") })
    }

    @Test
    public void configureExtensionWithExcludesTagExplicitlyAppendToDefault() {
        Project project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()
        project.apply plugin: PLUGIN_ID

        def configFile = testProjectDir.newFile('config.xml')
        project.extensions.spelling.externalConfigFile = configFile
        configFile.text = """\
            |<?xml version="1.0" ?>
            |<spelling>
            |    <excludes appendToDefault="true">
            |        <exclude pattern=".gradle/**/*" />
            |        <exclude pattern=".idea/**/*" />
            |    </excludes>
            |</spelling>""".stripMargin().stripIndent()
        project.evaluate()
        project.tasks.inspectSpelling.execute()
        assertEquals(3, project.extensions.spelling.excludes.size())
        assertTrue(project.extensions.spelling.excludes.any { it.equals("**/build/**/*") })
    }

    @Test
    public void configureExtensionWithIncludesTagNotAppendToDefault() {
        Project project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()
        project.apply plugin: PLUGIN_ID

        def configFile = testProjectDir.newFile('config.xml')
        project.extensions.spelling.externalConfigFile = configFile
        configFile.text = """\
            |<?xml version="1.0" ?>
            |<spelling>
            |    <includes appendToDefault="false">
            |        <include pattern="**/*.java" />
            |        <include pattern="**/*.groovy" />
            |    </includes>
            |</spelling>""".stripMargin().stripIndent()
        project.evaluate()
        project.tasks.inspectSpelling.execute()
        assertEquals(2, project.extensions.spelling.includes.size())
        assertFalse(project.extensions.spelling.includes.any { it.equals("**/*") })
    }

    @Test
    public void configureExtensionWithIncludesTagExplicitlyAppendToDefault() {
        Project project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()
        project.apply plugin: PLUGIN_ID

        def configFile = testProjectDir.newFile('config.xml')
        project.extensions.spelling.externalConfigFile = configFile
        configFile.text = """\
            |<?xml version="1.0" ?>
            |<spelling>
            |    <includes appendToDefault="true">
            |        <include pattern="**/*.java" />
            |        <include pattern="**/*.groovy" />
            |    </includes>
            |</spelling>""".stripMargin().stripIndent()
        project.evaluate()
        project.tasks.inspectSpelling.execute()
        assertEquals(3, project.extensions.spelling.includes.size())
        assertTrue(project.extensions.spelling.includes.any { it.equals("**/*") })
    }

    @Test
    public void configureExtensionWithIncludesTagWithoutAppendToDefault() {
        Project project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()
        project.apply plugin: PLUGIN_ID

        def configFile = testProjectDir.newFile('config.xml')
        project.extensions.spelling.externalConfigFile = configFile
        configFile.text = """\
            |<?xml version="1.0" ?>
            |<spelling>
            |    <includes>
            |        <include pattern="**/*.java" />
            |        <include pattern="**/*.groovy" />
            |    </includes>
            |</spelling>""".stripMargin().stripIndent()
        project.evaluate()
        project.tasks.inspectSpelling.execute()
        assertEquals(3, project.extensions.spelling.includes.size())
        assertTrue(project.extensions.spelling.includes.any { it.equals("**/*") })
    }
}
