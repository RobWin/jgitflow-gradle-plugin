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
package io.github.robwin.jgitflow

import com.atlassian.jgitflow.core.JGitFlow
import io.github.robwin.jgitflow.tasks.InitJGitflowTask
import io.github.robwin.jgitflow.tasks.ReleaseFinishTask
import io.github.robwin.jgitflow.tasks.ReleaseStartTask
import io.github.robwin.jgitflow.tasks.helper.ArtifactHelper
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
    static final RELEASE_VERSION_1 = '0.0.1'
    static final RELEASE_VERSION_2 = '0.0.2'
    static final RELEASE_VERSION_3 = '0.0.3'
    static final RELEASE_VERSION_4 = '0.0.4'
    static final RELEASE_VERSION_5 = '0.0.5'
    static final RELEASE_VERSION_6 = '0.2.0'
    static final NEW_VERSION_1 = '0.0.2-SNAPSHOT'
    static final NEW_VERSION_2 = '0.0.3-SNAPSHOT'
    static final NEW_VERSION_3 = '0.0.4-SNAPSHOT'
    static final NEW_VERSION_4 = '0.0.5-SNAPSHOT'
    static final NEW_VERSION_5 = '0.1.0-SNAPSHOT'
    static final NEW_VERSION_6 = '0.2.1-SNAPSHOT'

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
        project.ext.set('gitUserName', GIT_USERNAME)
        project.ext.set('gitPassword', GIT_PASSWORD)
        project.pluginManager.apply 'io.github.robwin.jgitflow'

        // Delete the "userHome" empty folder that Gradle creates in the project directory
        File userHomeDir = new File(project.projectDir, "userHome")
        assert !userHomeDir.exists() || userHomeDir.deleteDir()
    }

    def setup() {
        setupProject()
    }

    def setupSpec() {
        // Override the gradle native dir so it won't be created in the local Git working directory
        System.setProperty('org.gradle.native.dir', System.getProperty('java.io.tmpdir'))

        setupGitRepositories()
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

    def dumpLogs(List<RevCommit> logEntries, String branchName) {
        println("branch = ${branchName}")
        logEntries.each() {
            println("${it.commitTime} - ${it.fullMessage}")
        }
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
        project.ext.set('releaseVersion', RELEASE_VERSION_1)

        ReleaseStartTask releaseStartTask = project.tasks.findByName(JGitflowPlugin.RELEASE_START_TASK_NAME)
        Ref releaseBranch;

        loadGradleProperties()

        expect:
        releaseStartTask != null
        gradleProperties.version == PROJECT_VERSION

        when:
        releaseStartTask.execute()
        loadGradleProperties()
        releaseBranch = findBranch(localGit, "${jGitFlow.releaseBranchPrefix}${RELEASE_VERSION_1}")

        then:
        notThrown(Throwable)
        releaseBranch != null
        gradleProperties.version == RELEASE_VERSION_1
    }

    def "Test ReleaseFinishTask WithPushReleasesTrue" () {
        setup:
        project.ext.set('releaseVersion', RELEASE_VERSION_1)
        project.ext.set('newVersion', NEW_VERSION_1)
        project.ext.set('pushRelease', "true")

        ReleaseFinishTask releaseFinishTask = project.tasks.findByName(JGitflowPlugin.RELEASE_FINISH_TASK_NAME)

        loadGradleProperties()

        expect:
        releaseFinishTask != null
        gradleProperties.version == RELEASE_VERSION_1

        when:
        releaseFinishTask.execute()
        loadGradleProperties()
        List<RevCommit> remoteMasterLog = log(remoteGit, jGitFlow.masterBranchName, 2)
        dumpLogs(remoteMasterLog, jGitFlow.masterBranchName)
        List<RevCommit> remoteDevelopLog = log(remoteGit, jGitFlow.developBranchName, 3)
        dumpLogs(remoteDevelopLog, jGitFlow.developBranchName)

        then:
        notThrown(Throwable)
        gradleProperties.version == NEW_VERSION_1
        remoteMasterLog.get(0).fullMessage == "Merge branch '${jGitFlow.releaseBranchPrefix}${RELEASE_VERSION_1}'"
        remoteMasterLog.get(1).fullMessage == "[JGitFlow Gradle Plugin] Updated ${Project.GRADLE_PROPERTIES} for v${RELEASE_VERSION_1} release"
        remoteDevelopLog.get(0).fullMessage == "[JGitFlow Gradle Plugin] Updated ${Project.GRADLE_PROPERTIES} to version '${NEW_VERSION_1}'"
        remoteDevelopLog.get(1).fullMessage == "Merge branch '${jGitFlow.masterBranchName}' into ${jGitFlow.developBranchName}"
        remoteDevelopLog.get(2).fullMessage == "Merge branch '${jGitFlow.releaseBranchPrefix}${RELEASE_VERSION_1}'"
    }

    def "Test ReleaseFinishTask WithPushReleasesFalse" () {
        setup:
        project.ext.set('releaseVersion', RELEASE_VERSION_2)
        project.ext.set('newVersion', NEW_VERSION_2)
        project.ext.set('pushRelease', "false")

        ReleaseStartTask releaseStartTask = project.tasks.findByName(JGitflowPlugin.RELEASE_START_TASK_NAME)
        ReleaseFinishTask releaseFinishTask = project.tasks.findByName(JGitflowPlugin.RELEASE_FINISH_TASK_NAME)

        loadGradleProperties()

        expect:
        releaseStartTask != null
        releaseFinishTask != null
        gradleProperties.version == NEW_VERSION_1

        when:
        // we wrap the releaseStart with sleeping for one second, because otherwise the timestamps of the commits
        // for the releaseStart and releaseFinish can be the same which can lead to a random order in the log.
        // Since we are using the log to assert that our test passes, random ordering can cause our tests to fail.
        // In practical usage, there will be time between a releaseStart and releaseFinish, so adding time between
        // these calls is acceptable.
        Thread.sleep(1000)
        releaseStartTask.execute()
        loadGradleProperties()
        Thread.sleep(1000)

        then:
        notThrown(Throwable)
        gradleProperties.version == RELEASE_VERSION_2

        when:
        releaseFinishTask.execute()
        loadGradleProperties()

        List<RevCommit> remoteMasterLog = log(remoteGit, jGitFlow.masterBranchName, 2)
        //dumpLogs(remoteMasterLog, jGitFlow.masterBranchName)
        List<RevCommit> remoteDevelopLog = log(remoteGit, jGitFlow.developBranchName, 3)
        //dumpLogs(remoteDevelopLog, jGitFlow.developBranchName)
        List<RevCommit> localMasterLog = log(localGit, jGitFlow.masterBranchName, 2)
        //dumpLogs(localMasterLog, jGitFlow.masterBranchName)
        List<RevCommit> localDevelopLog = log(localGit, jGitFlow.developBranchName, 3)
        //dumpLogs(localDevelopLog, jGitFlow.developBranchName)

        then:
        notThrown(Throwable)
        gradleProperties.version == NEW_VERSION_2
        // check the remote git logs. We should not have a record of the changes for this release because we did not push
        remoteMasterLog.get(0).fullMessage == "Merge branch '${jGitFlow.releaseBranchPrefix}${RELEASE_VERSION_1}'"
        remoteMasterLog.get(1).fullMessage == "[JGitFlow Gradle Plugin] Updated ${Project.GRADLE_PROPERTIES} for v${RELEASE_VERSION_1} release"
        remoteDevelopLog.get(0).fullMessage == "[JGitFlow Gradle Plugin] Updated ${Project.GRADLE_PROPERTIES} to version '${NEW_VERSION_1}'"
        remoteDevelopLog.get(1).fullMessage == "Merge branch '${jGitFlow.masterBranchName}' into ${jGitFlow.developBranchName}"
        remoteDevelopLog.get(2).fullMessage == "Merge branch '${jGitFlow.releaseBranchPrefix}${RELEASE_VERSION_1}'"
        // check the local git logs. We should have a record of the changes for this release
        localMasterLog.get(0).fullMessage == "Merge branch '${jGitFlow.releaseBranchPrefix}${RELEASE_VERSION_2}'"
        localMasterLog.get(1).fullMessage == "[JGitFlow Gradle Plugin] Updated ${Project.GRADLE_PROPERTIES} for v${RELEASE_VERSION_2} release"
        localDevelopLog.get(0).fullMessage == "[JGitFlow Gradle Plugin] Updated ${Project.GRADLE_PROPERTIES} to version '${NEW_VERSION_2}'"
        localDevelopLog.get(1).fullMessage == "Merge branch '${jGitFlow.masterBranchName}' into ${jGitFlow.developBranchName}"
        localDevelopLog.get(2).fullMessage == "Merge branch '${jGitFlow.releaseBranchPrefix}${RELEASE_VERSION_2}'"
    }

    def "Test ReleaseFinishTask WithPushReleasesNotSet" () {
        setup:
        project.ext.set('releaseVersion', RELEASE_VERSION_3)
        project.ext.set('newVersion', NEW_VERSION_3)

        ReleaseStartTask releaseStartTask = project.tasks.findByName(JGitflowPlugin.RELEASE_START_TASK_NAME)
        ReleaseFinishTask releaseFinishTask = project.tasks.findByName(JGitflowPlugin.RELEASE_FINISH_TASK_NAME)

        loadGradleProperties()

        expect:
        releaseStartTask != null
        releaseFinishTask != null
        gradleProperties.version == NEW_VERSION_2

        when:
        // we wrap the releaseStart with sleeping for one second, because otherwise the timestamps of the commits
        // for the releaseStart and releaseFinish can be the same which can lead to a random order in the log.
        // Since we are using the log to assert that our test passes, random ordering can cause our tests to fail.
        // In practical usage, there will be time between a releaseStart and releaseFinish, so adding time between
        // these calls is acceptable.
        Thread.sleep(1000)
        releaseStartTask.execute()
        loadGradleProperties()
        Thread.sleep(1000)

        then:
        notThrown(Throwable)
        gradleProperties.version == RELEASE_VERSION_3

        when:
        releaseFinishTask.execute()
        loadGradleProperties()
        List<RevCommit> remoteMasterLog = log(remoteGit, jGitFlow.masterBranchName, 2)
        //dumpLogs(remoteMasterLog, jGitFlow.masterBranchName)
        List<RevCommit> remoteDevelopLog = log(remoteGit, jGitFlow.developBranchName, 3)
        //dumpLogs(remoteDevelopLog, jGitFlow.developBranchName)

        then:
        notThrown(Throwable)
        gradleProperties.version == NEW_VERSION_3
        remoteMasterLog.get(0).fullMessage == "Merge branch '${jGitFlow.releaseBranchPrefix}${RELEASE_VERSION_3}'"
        remoteMasterLog.get(1).fullMessage == "[JGitFlow Gradle Plugin] Updated ${Project.GRADLE_PROPERTIES} for v${RELEASE_VERSION_3} release"
        remoteDevelopLog.get(0).fullMessage == "[JGitFlow Gradle Plugin] Updated ${Project.GRADLE_PROPERTIES} to version '${NEW_VERSION_3}'"
        remoteDevelopLog.get(1).fullMessage == "Merge branch '${jGitFlow.masterBranchName}' into ${jGitFlow.developBranchName}"
        remoteDevelopLog.get(2).fullMessage == "Merge branch '${jGitFlow.releaseBranchPrefix}${RELEASE_VERSION_3}'"
    }

    def "Test ReleaseFinishTask WithPushReleasesNotSet WithReleaseVersionNotSet WithNewVersionNotSet" () {
        setup:

        ReleaseStartTask releaseStartTask = project.tasks.findByName(JGitflowPlugin.RELEASE_START_TASK_NAME)
        ReleaseFinishTask releaseFinishTask = project.tasks.findByName(JGitflowPlugin.RELEASE_FINISH_TASK_NAME)

        loadGradleProperties()
        String startVersion = gradleProperties.version


        expect:
        releaseStartTask != null
        releaseFinishTask != null
        ArtifactHelper.isSnapshot(startVersion)

        when:
        // we wrap the releaseStart with sleeping for one second, because otherwise the timestamps of the commits
        // for the releaseStart and releaseFinish can be the same which can lead to a random order in the log.
        // Since we are using the log to assert that our test passes, random ordering can cause our tests to fail.
        // In practical usage, there will be time between a releaseStart and releaseFinish, so adding time between
        // these calls is acceptable.
        Thread.sleep(1000)
        releaseStartTask.execute()
        loadGradleProperties()
        Thread.sleep(1000)

        then:
        notThrown(Throwable)
        gradleProperties.version == RELEASE_VERSION_4

        when:
        releaseFinishTask.execute()
        loadGradleProperties()
        List<RevCommit> remoteMasterLog = log(remoteGit, jGitFlow.masterBranchName, 2)
        //dumpLogs(remoteMasterLog, jGitFlow.masterBranchName)
        List<RevCommit> remoteDevelopLog = log(remoteGit, jGitFlow.developBranchName, 3)
        //dumpLogs(remoteDevelopLog, jGitFlow.developBranchName)

        then:
        notThrown(Throwable)
        gradleProperties.version == NEW_VERSION_4
        remoteMasterLog.get(0).fullMessage == "Merge branch '${jGitFlow.releaseBranchPrefix}${RELEASE_VERSION_4}'"
        remoteMasterLog.get(1).fullMessage == "[JGitFlow Gradle Plugin] Updated ${Project.GRADLE_PROPERTIES} for v${RELEASE_VERSION_4} release"
        remoteDevelopLog.get(0).fullMessage == "[JGitFlow Gradle Plugin] Updated ${Project.GRADLE_PROPERTIES} to version '${NEW_VERSION_4}'"
        remoteDevelopLog.get(1).fullMessage == "Merge branch '${jGitFlow.masterBranchName}' into ${jGitFlow.developBranchName}"
        remoteDevelopLog.get(2).fullMessage == "Merge branch '${jGitFlow.releaseBranchPrefix}${RELEASE_VERSION_4}'"
    }

    def "Test ReleaseFinishTask WithPushReleasesNotSet WithReleaseVersionNotSet" () {
        setup:
        project.ext.set('newVersion', NEW_VERSION_5)

        ReleaseStartTask releaseStartTask = project.tasks.findByName(JGitflowPlugin.RELEASE_START_TASK_NAME)
        ReleaseFinishTask releaseFinishTask = project.tasks.findByName(JGitflowPlugin.RELEASE_FINISH_TASK_NAME)

        loadGradleProperties()
        String startVersion = gradleProperties.version


        expect:
        releaseStartTask != null
        releaseFinishTask != null
        ArtifactHelper.isSnapshot(startVersion)

        when:
        // we wrap the releaseStart with sleeping for one second, because otherwise the timestamps of the commits
        // for the releaseStart and releaseFinish can be the same which can lead to a random order in the log.
        // Since we are using the log to assert that our test passes, random ordering can cause our tests to fail.
        // In practical usage, there will be time between a releaseStart and releaseFinish, so adding time between
        // these calls is acceptable.
        Thread.sleep(1000)
        releaseStartTask.execute()
        loadGradleProperties()
        Thread.sleep(1000)

        then:
        notThrown(Throwable)
        gradleProperties.version == RELEASE_VERSION_5

        when:
        releaseFinishTask.execute()
        loadGradleProperties()
        List<RevCommit> remoteMasterLog = log(remoteGit, jGitFlow.masterBranchName, 2)
        //dumpLogs(remoteMasterLog, jGitFlow.masterBranchName)
        List<RevCommit> remoteDevelopLog = log(remoteGit, jGitFlow.developBranchName, 3)
        //dumpLogs(remoteDevelopLog, jGitFlow.developBranchName)

        then:
        notThrown(Throwable)
        gradleProperties.version == NEW_VERSION_5
        remoteMasterLog.get(0).fullMessage == "Merge branch '${jGitFlow.releaseBranchPrefix}${RELEASE_VERSION_5}'"
        remoteMasterLog.get(1).fullMessage == "[JGitFlow Gradle Plugin] Updated ${Project.GRADLE_PROPERTIES} for v${RELEASE_VERSION_5} release"
        remoteDevelopLog.get(0).fullMessage == "[JGitFlow Gradle Plugin] Updated ${Project.GRADLE_PROPERTIES} to version '${NEW_VERSION_5}'"
        remoteDevelopLog.get(1).fullMessage == "Merge branch '${jGitFlow.masterBranchName}' into ${jGitFlow.developBranchName}"
        remoteDevelopLog.get(2).fullMessage == "Merge branch '${jGitFlow.releaseBranchPrefix}${RELEASE_VERSION_5}'"
    }

    def "Test ReleaseFinishTask WithPushReleasesNotSet WithNewVersionNotSet" () {
        setup:
        project.ext.set('releaseVersion', RELEASE_VERSION_6)

        ReleaseStartTask releaseStartTask = project.tasks.findByName(JGitflowPlugin.RELEASE_START_TASK_NAME)
        ReleaseFinishTask releaseFinishTask = project.tasks.findByName(JGitflowPlugin.RELEASE_FINISH_TASK_NAME)

        loadGradleProperties()
        String startVersion = gradleProperties.version


        expect:
        releaseStartTask != null
        releaseFinishTask != null
        ArtifactHelper.isSnapshot(startVersion)

        when:
        // we wrap the releaseStart with sleeping for one second, because otherwise the timestamps of the commits
        // for the releaseStart and releaseFinish can be the same which can lead to a random order in the log.
        // Since we are using the log to assert that our test passes, random ordering can cause our tests to fail.
        // In practical usage, there will be time between a releaseStart and releaseFinish, so adding time between
        // these calls is acceptable.
        Thread.sleep(1000)
        releaseStartTask.execute()
        loadGradleProperties()
        Thread.sleep(1000)

        then:
        notThrown(Throwable)
        gradleProperties.version == RELEASE_VERSION_6

        when:
        releaseFinishTask.execute()
        loadGradleProperties()
        List<RevCommit> remoteMasterLog = log(remoteGit, jGitFlow.masterBranchName, 2)
        //dumpLogs(remoteMasterLog, jGitFlow.masterBranchName)
        List<RevCommit> remoteDevelopLog = log(remoteGit, jGitFlow.developBranchName, 3)
        //dumpLogs(remoteDevelopLog, jGitFlow.developBranchName)

        then:
        notThrown(Throwable)
        gradleProperties.version == NEW_VERSION_6
        remoteMasterLog.get(0).fullMessage == "Merge branch '${jGitFlow.releaseBranchPrefix}${RELEASE_VERSION_6}'"
        remoteMasterLog.get(1).fullMessage == "[JGitFlow Gradle Plugin] Updated ${Project.GRADLE_PROPERTIES} for v${RELEASE_VERSION_6} release"
        remoteDevelopLog.get(0).fullMessage == "[JGitFlow Gradle Plugin] Updated ${Project.GRADLE_PROPERTIES} to version '${NEW_VERSION_6}'"
        remoteDevelopLog.get(1).fullMessage == "Merge branch '${jGitFlow.masterBranchName}' into ${jGitFlow.developBranchName}"
        remoteDevelopLog.get(2).fullMessage == "Merge branch '${jGitFlow.releaseBranchPrefix}${RELEASE_VERSION_6}'"
    }
}
