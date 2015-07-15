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
package io.github.robwin.jgitflow.tasks
import com.atlassian.jgitflow.core.JGitFlow
import io.github.robwin.jgitflow.tasks.credentialsprovider.CredentialsProviderHelper
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.artifacts.Dependency
import org.gradle.api.tasks.TaskAction
import org.gradle.mvn3.org.apache.maven.artifact.ArtifactUtils

import static io.github.robwin.jgitflow.tasks.helper.GitHelper.commitGradlePropertiesFile
import static io.github.robwin.jgitflow.tasks.helper.GitHelper.updateGradlePropertiesFile

class ReleaseStartTask extends DefaultTask {

    @TaskAction
    void start(){

        String releaseVersion = project.property('releaseVersion')
        CredentialsProviderHelper.setupCredentialProvider(project)

        validateReleaseVersion(releaseVersion)

        JGitFlow flow = JGitFlow.get(project.rootProject.rootDir)

        //Make sure that the develop branch is used
        flow.git().checkout().setName(flow.getDevelopBranchName()).call()

        String allowSnapshotDependencies = project.hasProperty('allowSnapshotDependencies') ? project.property('allowSnapshotDependencies') : false

        if (!allowSnapshotDependencies) {
            //Check that no library dependency is a snapshot
            checkThatNoDependencyIsASnapshot()
        }

        //Start a release
        def command = flow.releaseStart(releaseVersion)
        if (project.hasProperty('baseCommit')) {
            String baseCommit = project.property('baseCommit')
            command.setStartCommit(baseCommit)
        }
        command.call()

        //Local working copy is now on release branch

        //Update the release version
        updateGradlePropertiesFile(project, releaseVersion)

        //Commit the release version
        commitGradlePropertiesFile(flow.git(), "[JGitFlow Gradle Plugin] Updated gradle.properties for v" + releaseVersion + " release")
    }

    private void validateReleaseVersion(String releaseVersion) {
        if(project.version == releaseVersion){
            throw new GradleException("Release version '${releaseVersion}' and current version '${project.version}' must not be equal.")
        }
        if(ArtifactUtils.isSnapshot(releaseVersion)){
            throw new GradleException("Release version must not be a snapshot version: ${releaseVersion}")
        }
    }

    private void checkThatNoDependencyIsASnapshot() {
        def snapshotDependencies = [] as Set
        project.allprojects.each { project ->
            project.configurations.each { configuration ->
                configuration.allDependencies.each { Dependency dependency ->
                    if (!dependency.group.equals(project.group) && ArtifactUtils.isSnapshot(dependency.version)) {
                        snapshotDependencies.add("${dependency.group}:${dependency.name}:${dependency.version}")
                    }
                }
            }
        }
        if (!snapshotDependencies.isEmpty()) {
            throw new GradleException("Cannot start a release due to snapshot dependencies: ${snapshotDependencies}")
        }
    }

}
