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
import org.eclipse.jgit.api.MergeResult
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

class FeatureFinishTask extends DefaultTask {

    @TaskAction
    void finish(){
        String featureName = project.property('featureName')
        CredentialsProviderHelper.setupCredentialProvider(project)
        JGitFlow flow = JGitFlow.get(project.rootProject.rootDir)

        // adding scmMessagePrefix into feature finish task
        String scmMessagePrefix
        if (project.hasProperty('scmMessagePrefix')) {
            scmMessagePrefix = project.property('scmMessagePrefix')
            flow.featureFinish(releaseVersion).setScmMessagePrefix(scmMessagePrefix)
        }else{
            scmMessagePrefix = "[JGitFlow Gradle Plugin]"
            flow.featureFinish(releaseVersion).setScmMessagePrefix(scmMessagePrefix)
        }

        // adding scmMessageSuffix into feature finish task
        String scmMessageSuffix
        if (project.hasProperty('scmMessageSuffix')) {
            scmMessageSuffix = project.property('scmMessageSuffix')
            flow.featureFinish(releaseVersion).setScmMessageSuffix(scmMessageSuffix)
        }else{
            scmMessageSuffix = "[JGitFlow Gradle Plugin]"
            flow.featureFinish(releaseVersion).setScmMessageSuffix(scmMessageSuffix)
        }

        MergeResult mergeResult = flow.featureFinish(featureName).call();
        if (!mergeResult.getMergeStatus().isSuccessful())
        {
            getLogger().error("Error merging into " + flow.getDevelopBranchName() + ":");
            getLogger().error(mergeResult.toString());
            throw new GradleException("Error while merging feature!");
        }
        flow.git().close()
    }
}
