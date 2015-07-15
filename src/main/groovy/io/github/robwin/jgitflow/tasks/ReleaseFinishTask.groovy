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
import com.atlassian.jgitflow.core.ReleaseMergeResult
import io.github.robwin.jgitflow.tasks.credentialsprovider.CredentialsProviderHelper
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import static io.github.robwin.jgitflow.tasks.helper.GitHelper.commitGradlePropertiesFile
import static io.github.robwin.jgitflow.tasks.helper.GitHelper.updateGradlePropertiesFile

class ReleaseFinishTask extends DefaultTask {

    @TaskAction
    void finish(){
        String releaseVersion = project.property('releaseVersion')
        String newVersion = project.property('newVersion')
        CredentialsProviderHelper.setupCredentialProvider(project)
        JGitFlow flow = JGitFlow.get(project.rootProject.rootDir)
        ReleaseMergeResult mergeResult = flow.releaseFinish(releaseVersion).call();
        if (!mergeResult.wasSuccessful())
        {
            if (mergeResult.masterHasProblems())
            {
                logger.error("Error merging into " + flow.getMasterBranchName() + ":");
                logger.error(mergeResult.getMasterResult().toString());
            }

            if (mergeResult.developHasProblems())
            {
                logger.error("Error merging into " + flow.getDevelopBranchName() + ":");
                logger.error(mergeResult.getDevelopResult().toString());
            }
            throw new GradleException("Error while merging release!");
        }
        //Local working copy is now on develop branch

        //Update the develop version to the new version
        updateGradlePropertiesFile(project, newVersion)

        //Commit the release version
        commitGradlePropertiesFile(flow.git(), "[JGitFlow Gradle Plugin] Updated gradle.properties to version '${newVersion}'")

        flow.git().push().call();

    }
}
