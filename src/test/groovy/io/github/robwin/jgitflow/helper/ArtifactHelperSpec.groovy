/*
 *
 *  Copyright 2016 Robert Winkler
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package io.github.robwin.jgitflow.helper

import io.github.robwin.jgitflow.tasks.helper.ArtifactHelper
import org.gradle.api.GradleException
import spock.lang.Specification
import spock.lang.Unroll

class ArtifactHelperSpec extends Specification{

    @Unroll
    def "newSnapshotVersion increments returns #newVersion for paramters #oldVersion  #newVersionIncrement"() {

        expect:
        ArtifactHelper.newSnapshotVersion(oldVersion, newVersionIncrement) == newVersion

        where:
        oldVersion    | newVersionIncrement | newVersion

        "1.0.0"       | "PATCH"             | "1.0.1-SNAPSHOT"
        "2.1.4"       | "PATCH"             | "2.1.5-SNAPSHOT"
        "3.2.1"       | "patch"             | "3.2.2-SNAPSHOT"
        "3.12.13"     | "patch"             | "3.12.14-SNAPSHOT"

        "1.2.3"       | "MINOR"             | "1.3.0-SNAPSHOT"
        "2.4.9"       | "MINOR"             | "2.5.0-SNAPSHOT"
        "3.11.0"      | "MINOR"             | "3.12.0-SNAPSHOT"

        "1.2.3"       | "MAJOR"             | "2.0.0-SNAPSHOT"
        "9.1.0"       | "MAJOR"             | "10.0.0-SNAPSHOT"
    }

    @Unroll
    def "newSnapshotVersion throws exception when newVersionIncrement is #newVersionIncrement"() {

        when:
        ArtifactHelper.newSnapshotVersion("1.0.0", newVersionIncrement)

        then:
        thrown GradleException

        where:
        newVersionIncrement << ["", "test", "xyx"]
    }


}
