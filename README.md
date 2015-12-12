# gradle-spelling-plugin

[![Build Status](https://travis-ci.org/ksoichiro/gradle-spelling-plugin.svg?branch=master)](https://travis-ci.org/ksoichiro/gradle-spelling-plugin)
[![Coverage Status](https://coveralls.io/repos/ksoichiro/gradle-spelling-plugin/badge.svg?branch=master&service=github)](https://coveralls.io/github/ksoichiro/gradle-spelling-plugin?branch=master)

Gradle plugin to inspect spelling using custom blacklist.

## Usage

Apply plugin in build.gradle:

```gradle
// Gradle 2.1+
plugins {
    id "com.github.ksoichiro.spelling" version "0.0.1"
}

// Gradle 2.0 and former
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.github.ksoichiro:gradle-spelling-plugin:0.0.1'
    }
}

apply plugin: 'com.github.ksoichiro.spelling'

// If you use SNAPSHOT version
buildscript {
    repositories {
        jcenter()
        maven {
            url uri('https://oss.sonatype.org/content/repositories/snapshots/')
        }
    }
    dependencies {
        classpath 'com.github.ksoichiro:gradle-spelling-plugin:0.0.1-SNAPSHOT'
    }
}

apply plugin: 'com.github.ksoichiro.spelling'
```

Configure plugin to define rules in your build.gradle.

```gradle
spelling {
    excludes += 'build.gradle'
    definition {
        rules {
            define forbidden: 'Foo', recommended: 'Bar'
        }
    }
}
```

Then, execute `inspectSpelling` task:

```sh
$ ./gradlew inspectSpelling
./gradlew inspectSpelling
:inspectSpelling
/path/to/your/project/src/main/java/com/example/A.java:4: Error: Found 'Foo', should replace to 'Bar'.
:inspectSpelling FAILED

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':inspectSpelling'.
> Spelling inspection failed: 1 violations found
```


## Configuration

```gradle
// Most of the configurations are optional.
// At least you should configure 'definition.rules'
// to define your spelling rules.
spelling {
    // If you want to continue build even when
    // some violations found, then set failOnError to false
    failOnError false

    // If you want to fix violations, then set
    // fixAutomatically to true
    fixAutomatically false

    // If you want to customize error message,
    // set 'message' with String.format() format.
    // '%1s' will be the forbidden word,
    // and '%2s' will be the recommended word.
    message "%1s found but it should be %2s."

    // If you want to include only some part of
    // the project for inspection, set 'includes' array.
    // 'includes' is ["**/*"] by default.
    includes ['**/src/**/*']

    // If you want to exclude some files from inspection,
    // add paths to 'excludes' array.
    // 'excludes' is ["**/build/**/*"] by default.
    excludes += 'build.gradle'

    definition {
        // Define your inspection rules.
        rules {
            // When 'forbidden' expression found in files,
            // it is assumed as a violation.
            // 'recommended' expression will be shown
            // in the violation error message.
            define forbidden: 'Foo', recommended: 'Bar'
        }
    }
}
```

You can also configure with external XML file.

```gradle
spelling {
    externalConfigFile file('config.xml')
}
```

The config.xml will look like this:

```xml
<?xml version="1.0" ?>
<spelling>
    <message>Error: Found '%1s' but is should be '%2s'.</message>
    <failOnError>false</failOnError>
    <excludes>
        <exclude>config.xml</exclude>
    </excludes>
    <definition>
        <rules>
            <rule forbidden="Foo" recommended="Bar" />
        </rules>
    </definition>
</spelling>
```

## TODO

* [x] External configuration file (XML format)
* [ ] Inspection report
* [x] Include/exclude some files or directories
* [ ] Configuration for each file extension (e.g. .java)
* [ ] Configuration for specific files and directories
* [ ] Exclude configuration from .gitignore

## License

    Copyright 2015 Soichiro Kashima

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
