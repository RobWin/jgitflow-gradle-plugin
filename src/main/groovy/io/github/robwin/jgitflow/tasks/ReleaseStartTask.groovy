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
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.Status
import org.eclipse.jgit.api.errors.GitAPIException
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.mvn3.org.apache.maven.artifact.ArtifactUtils

class ReleaseStartTask extends DefaultTask {

    @Input
    String releaseVersion;

    @Input
    @Optional
    String baseCommit;

    @Input
    @Optional
    boolean allowSnapshotDependencies

    @TaskAction
    void start(){

        validateReleaseVersion()

        JGitFlow flow = JGitFlow.get(project.rootProject.rootDir)

        //Make sure that the develop branch is used
        flow.git().checkout().setName(flow.getDevelopBranchName()).call()

        if(!allowSnapshotDependencies){
            //Check that no library dependency is a snapshot
            checkThatNoDependencyIsASnapshot()
        }

        //Start a release
        def command = flow.releaseStart(releaseVersion)
        if (baseCommit) {
            command.setStartCommit(baseCommit)
        }
        command.call()

        //Local working copy is now on release branch

        //Update the release version
        updateGradlePropertiesFile(releaseVersion)

        //Commit the release version
        commitGradlePropertiesFile(flow.git(), "[JGitFlow Gradle Plugin] Updated gradle.properties for v" + releaseVersion + " release")
    }

    private void validateReleaseVersion() {
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

    private void updateGradlePropertiesFile(String releaseVersion)
    {
        String currentVersion = project.version
        File propertiesFile = project.file(Project.GRADLE_PROPERTIES)
        if (!propertiesFile.file) {
            propertiesFile.append("version=${releaseVersion}")
        }else {
            project.ant.replace(file: propertiesFile, token: "version=${currentVersion}", value: "version=${releaseVersion}", failOnNoReplacements: true)
        }
    }

    private void commitGradlePropertiesFile(Git git, String message) {
        try {
            Status status = git.status().call()
            if (!status.isClean()) {
                //logger.info("Added ${project.file(Project.GRADLE_PROPERTIES).absolutePath}")
                git.add().addFilepattern(".").call()
                git.commit().setMessage(message).call()
            }
        }catch (GitAPIException e) {
            throw new GradleException("Failed to commit gradle.properties: ${e.message}", e)
        }
    }
}
