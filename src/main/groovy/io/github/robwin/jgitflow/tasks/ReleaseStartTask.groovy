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
package io.github.robwin.jgitflow.tasks
import com.atlassian.jgitflow.core.JGitFlow
import io.github.robwin.jgitflow.tasks.credentialsprovider.CredentialsProviderHelper
import io.github.robwin.jgitflow.tasks.helper.ArtifactHelper
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.tasks.TaskAction

import static io.github.robwin.jgitflow.tasks.helper.GitHelper.commitGradlePropertiesFile
import static io.github.robwin.jgitflow.tasks.helper.GitHelper.updateGradlePropertiesFile

class ReleaseStartTask extends AbstractCommandTask {

    @TaskAction
    void start(){

        String releaseVersion = project.properties['releaseVersion']?:loadVersionFromGradleProperties()
        CredentialsProviderHelper.setupCredentialProvider(project)

        validateReleaseVersion(releaseVersion)

        JGitFlow flow = JGitFlow.get(project.rootProject.rootDir)

        //Make sure that the develop branch is used
        flow.git().checkout().setName(flow.getDevelopBranchName()).call()

        String allowSnapshotDependencies = project.hasProperty('allowSnapshotDependencies') ? project.property('allowSnapshotDependencies') : false
        //Configuration property for
        String allowProjectGroupSnapshotDependencies = project.hasProperty('allowProjectGroupSnapshotDependencies') ? project.property('allowProjectGroupSnapshotDependencies') : true

        if (!allowSnapshotDependencies) {
            //Check that no library dependency is a snapshot
            checkThatNoDependencyIsASnapshot(allowProjectGroupSnapshotDependencies)
        }

        //Start a release
        def command = flow.releaseStart(releaseVersion)

        setCommandPrefixAndSuffix(command)

        if (project.hasProperty('baseCommit')) {
            String baseCommit = project.property('baseCommit')
            command.setStartCommit(baseCommit)
        }

        command.call()

        //Local working copy is now on release branch

        //Update the release version
        updateGradlePropertiesFile(project, releaseVersion)

        //Commit the release version
        commitGradlePropertiesFile(flow.git(), getScmMessagePrefix(command) + "Updated gradle.properties for v" + releaseVersion + " release" + getScmMessageSuffix(command))

        flow.git().close()
    }

    private String loadVersionFromGradleProperties() {
        if(!project.hasProperty('version')) {
            throw new GradleException('version or releaseVersion property have to be present')
        }
        String version = project.property('version') as String
        if (version == "unspecified") {
            throw new GradleException("Cannot get version property from ${Project.GRADLE_PROPERTIES}")
        }
        
        ArtifactHelper.removeSnapshot(version)
    }

    private void validateReleaseVersion(String releaseVersion) {
        if(project.version == releaseVersion){
            throw new GradleException("Release version '${releaseVersion}' and current version '${project.version}' must not be equal.")
        }
        if(ArtifactHelper.isSnapshot(releaseVersion)){
            throw new GradleException("Release version must not be a snapshot version: ${releaseVersion}")
        }
    }

    private void checkThatNoDependencyIsASnapshot(String allowProjectGroupSnapshotDependencies) {
        def snapshotDependencies = [] as Set
        project.allprojects.each { project ->
            project.configurations.each { configuration ->
                configuration.allDependencies.each { Dependency dependency ->
                    if ((!allowProjectGroupSnapshotDependencies || !dependency.group.equals(project.group)) && ArtifactHelper.isSnapshot(dependency.version)) {
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
