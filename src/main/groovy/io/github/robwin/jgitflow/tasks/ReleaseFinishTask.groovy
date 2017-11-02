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
import com.atlassian.jgitflow.core.ReleaseMergeResult
import io.github.robwin.jgitflow.tasks.credentialsprovider.CredentialsProviderHelper
import io.github.robwin.jgitflow.tasks.helper.ArtifactHelper
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import static io.github.robwin.jgitflow.tasks.helper.GitHelper.commitGradlePropertiesFile
import static io.github.robwin.jgitflow.tasks.helper.GitHelper.updateGradlePropertiesFile

class ReleaseFinishTask extends DefaultTask {

    @TaskAction
    void finish(){
        String releaseVersion = project.properties['releaseVersion']?:loadVersionFromGradleProperties()
        String newVersion = project.properties['newVersion']?:ArtifactHelper.newSnapshotVersion(releaseVersion)
        boolean pushRelease = true
        if (project.properties.containsKey('pushRelease')) {
            pushRelease = Boolean.valueOf(project.property('pushRelease') as String)
        }
        CredentialsProviderHelper.setupCredentialProvider(project)
        JGitFlow flow = JGitFlow.get(project.rootProject.rootDir)

        // adding scmMessagePrefix into release start task
        String scmMessagePrefix
        if (project.hasProperty('scmMessagePrefix')) {
            scmMessagePrefix = project.property('scmMessagePrefix')
            flow.releaseFinish(releaseVersion).setScmMessagePrefix(scmMessagePrefix)
        }else{
            scmMessagePrefix = ""
        }

        // adding scmMessageSuffix into release start task
        String scmMessageSuffix
        if (project.hasProperty('scmMessageSuffix')) {
            scmMessageSuffix = project.property('scmMessageSuffix')
            flow.releaseFinish(releaseVersion).setScmMessageSuffix(scmMessageSuffix)
        }else{
            scmMessageSuffix = ""
        }

        ReleaseMergeResult mergeResult = flow.releaseFinish(releaseVersion).call();
        if (!mergeResult.wasSuccessful())
        {
            if (mergeResult.masterHasProblems())
            {
                logger.error("Error merging into " + flow.getMasterBranchName() + ":")
                logger.error(mergeResult.getMasterResult().toString());
            }

            if (mergeResult.developHasProblems())
            {
                logger.error("Error merging into " + flow.getDevelopBranchName() + ":")
                logger.error(mergeResult.getDevelopResult().toString());
            }
            throw new GradleException("Error while merging release!");
        }
        //Local working copy is now on develop branch

        //Update the develop version to the new version
        updateGradlePropertiesFile(project, newVersion)

        //Commit the release version
        commitGradlePropertiesFile(flow.git(), scmMessagePrefix + " [JGitFlow Gradle Plugin] Updated gradle.properties to version '${newVersion}' " +scmMessageSuffix )

        if (pushRelease) {
            flow.git().push().setPushAll().setPushTags().call();
        }

        flow.git().close()
    }


    private String loadVersionFromGradleProperties() {
        if(!project.hasProperty('version')) {
            throw new GradleException('version or releaseVersion property have to be present')
        }
        ArtifactHelper.removeSnapshot(project.property('version') as String)
    }
}
