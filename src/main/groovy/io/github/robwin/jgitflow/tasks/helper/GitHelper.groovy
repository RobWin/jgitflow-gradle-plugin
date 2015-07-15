/*
 *
 *  Copyright 2015 Robert Winkler
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
package io.github.robwin.jgitflow.tasks.helper

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.Status
import org.eclipse.jgit.api.errors.GitAPIException
import org.gradle.api.GradleException
import org.gradle.api.Project

class GitHelper {

    public static void updateGradlePropertiesFile(Project project, String version)
    {
        String currentVersion = project.version
        File propertiesFile = project.file(Project.GRADLE_PROPERTIES)
        if (!propertiesFile.file) {
            propertiesFile.append("version=${version}")
        }else {
            project.ant.replace(file: propertiesFile, token: "version=${currentVersion}", value: "version=${version}", failOnNoReplacements: true)
        }
    }

    public static void commitGradlePropertiesFile(Git git, String message) {
        try {
            Status status = git.status().call()
            if (!status.isClean()) {
                git.add().addFilepattern(".").call()
                git.commit().setMessage(message).call()
            }
        }catch (GitAPIException e) {
            throw new GradleException("Failed to commit gradle.properties: ${e.message}", e)
        }
    }
}
