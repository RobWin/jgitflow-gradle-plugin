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
package io.github.robwin.jgitflow

import com.atlassian.jgitflow.core.JGitFlow
import io.github.robwin.jgitflow.tasks.InitJGitflowTask
import io.github.robwin.jgitflow.tasks.ReleaseFinishTask
import io.github.robwin.jgitflow.tasks.ReleaseStartTask
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.revwalk.RevCommit
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Shared
import spock.lang.Specification

import static io.github.robwin.jgitflow.tasks.helper.GitHelper.*

class JGitflowTaskSpec extends Specification{

    static final PROJECT_NAME = 'testProject'
    static final GIT_USERNAME = 'gitusername'
    static final GIT_PASSWORD = 'gitpassword'
    static final PROJECT_VERSION = '0.0.1-SNAPSHOT'
    static final RELEASE_VERSION = '0.0.1'
    static final NEW_VERSION = '0.0.2-SNAPSHOT'

    @Shared JGitFlow jGitFlow

    @Shared Project project

    @Shared Git tempGit

    @Shared Git remoteGit

    @Shared Git localGit

    @Shared Properties gradleProperties = new Properties();

    def loadGradleProperties() {
        Reader fileReader = project.file(Project.GRADLE_PROPERTIES).newReader()
        gradleProperties.load(fileReader)
        fileReader.close()

        project.version = gradleProperties.version
    }

    def setupGitRepositories() {
        // Prepare the remote Git repo
        remoteGit = createTempRepository("${PROJECT_NAME}Remote", true)

        // Prepare a temporary local Git repo to commit some changes to and push them to the remote repo
        tempGit = createTempRepository('tempRepo', false)
        addRemote(tempGit, 'origin', remoteGit.repository.directory.absolutePath)

        File gradlePropertiesFile = new File(tempGit.repository.directory.parentFile, Project.GRADLE_PROPERTIES)
        gradlePropertiesFile << "version=0.0.1-SNAPSHOT"
        add(tempGit, ".")
        commit(tempGit, 'Project setup.')
        push(tempGit)

        createBranch(tempGit, 'develop')
        checkout(tempGit, 'develop')

        randomCommit(tempGit, 'a.txt')
        randomCommit(tempGit, 'a.txt')
        randomCommit(tempGit, 'b.txt')

        push(tempGit)

        // Prepare the local (project) directory, the JGitflow will operate on during the test
        localGit = createTempRepository(PROJECT_NAME, false)
        addRemote(localGit, 'origin', remoteGit.repository.directory.absolutePath)
    }

    def setupProject() {
        project = ProjectBuilder.builder().withName(PROJECT_NAME)
                .withProjectDir(localGit.repository.directory.parentFile)
                .build()
        project.version = PROJECT_VERSION
        project.ext.set('releaseVersion', RELEASE_VERSION)
        project.ext.set('newVersion', NEW_VERSION)
        project.ext.set('gitUserName', GIT_USERNAME)
        project.ext.set('gitPassword', GIT_PASSWORD)
        project.pluginManager.apply 'io.github.robwin.jgitflow'

        // Delete the "userHome" empty folder that Gradle creates in the project directory
        File userHomeDir = new File(project.projectDir, "userHome")
        assert !userHomeDir.exists() || userHomeDir.deleteDir()
    }

    def setupSpec() {
        // Override the gradle native dir so it won't be created in the local Git working directory
        System.setProperty('org.gradle.native.dir', System.getProperty('java.io.tmpdir'))

        setupGitRepositories()
        setupProject()
    }

    def cleanupSpec() {
        jGitFlow.git().close()
        tempGit.close()
        tempGit.repository.directory.parentFile.deleteDir()
        localGit.close()
        localGit.repository.directory.parentFile.deleteDir()
        remoteGit.close()
        remoteGit.repository.directory.deleteDir()
    }


    def "Test InitJGitflowTask"() {
        InitJGitflowTask initJGitflowTask = project.tasks.findByName(JGitflowPlugin.INIT_JGTIFLOW_TASK_NAME)

        expect:
        initJGitflowTask != null

        when:
        initJGitflowTask.init()
        jGitFlow = JGitFlow.get(project.rootProject.rootDir)

        then:
        notThrown(Throwable)
        jGitFlow.masterBranchName == initJGitflowTask.master
        jGitFlow.developBranchName == initJGitflowTask.develop
        jGitFlow.releaseBranchPrefix == initJGitflowTask.release
        jGitFlow.featureBranchPrefix == initJGitflowTask.feature
        jGitFlow.hotfixBranchPrefix == initJGitflowTask.hotfix
        jGitFlow.versionTagPrefix == initJGitflowTask.versiontag
    }

    def "Test ReleaseStartTask"() {
        setup:
        ReleaseStartTask releaseStartTask = project.tasks.findByName(JGitflowPlugin.RELEASE_START_TASK_NAME)
        Ref releaseBranch;

        loadGradleProperties()

        expect:
        releaseStartTask != null
        gradleProperties.version == PROJECT_VERSION

        when:
        releaseStartTask.execute()
        loadGradleProperties()
        releaseBranch = findBranch(localGit, "${jGitFlow.releaseBranchPrefix}${RELEASE_VERSION}")

        then:
        notThrown(Throwable)
        releaseBranch != null
        gradleProperties.version == RELEASE_VERSION
    }

    def "Test ReleaseFinishTask" () {
        setup:
        ReleaseFinishTask releaseFinishTask = project.tasks.findByName(JGitflowPlugin.RELEASE_FINISH_TASK_NAME)

        loadGradleProperties()

        expect:
        releaseFinishTask != null
        gradleProperties.version == RELEASE_VERSION

        when:
        releaseFinishTask.execute()
        loadGradleProperties()
        List<RevCommit> remoteMasterLog = log(remoteGit, jGitFlow.masterBranchName, 2)
        List<RevCommit> remoteDevelopLog = log(remoteGit, jGitFlow.developBranchName, 3)

        then:
        notThrown(Throwable)
        gradleProperties.version == NEW_VERSION
        remoteMasterLog.get(0).fullMessage == "Merge branch '${jGitFlow.releaseBranchPrefix}${RELEASE_VERSION}'"
        remoteMasterLog.get(1).fullMessage == "[JGitFlow Gradle Plugin] Updated ${Project.GRADLE_PROPERTIES} for v${RELEASE_VERSION} release"
        remoteDevelopLog.get(0).fullMessage == "[JGitFlow Gradle Plugin] Updated ${Project.GRADLE_PROPERTIES} to version '${NEW_VERSION}'"
        remoteDevelopLog.get(1).fullMessage == "Merge branch '${jGitFlow.masterBranchName}' into ${jGitFlow.developBranchName}"
        remoteDevelopLog.get(2).fullMessage == "Merge branch '${jGitFlow.releaseBranchPrefix}${RELEASE_VERSION}'"
    }
}
