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
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class FeaturePublishTask extends DefaultTask {

    @TaskAction
    void publish(){
        String featureName = project.property('featureName')
        CredentialsProviderHelper.setupCredentialProvider(project)
        JGitFlow flow = JGitFlow.get(project.rootProject.rootDir)

        // adding scmMessagePrefix into feature publish task
        String scmMessagePrefix
        if (project.hasProperty('scmMessagePrefix')) {
            scmMessagePrefix = project.property('scmMessagePrefix')
            flow.featurePublish(featureName).setScmMessagePrefix(scmMessagePrefix)
        }else{
            scmMessagePrefix = "[Gradle Plugin PREFIX]"
            flow.featurePublish(featureName).setScmMessagePrefix(scmMessagePrefix)
        }

        // adding scmMessageSuffix into feature finish task
        String scmMessageSuffix
        if (project.hasProperty('scmMessageSuffix')) {
            scmMessageSuffix = project.property('scmMessageSuffix')
            flow.featurePublish(featureName).setScmMessageSuffix(scmMessageSuffix)
        }else{
            scmMessageSuffix = "[Gradle Plugin SUFFIX]"
            flow.featurePublish(featureName).setScmMessageSuffix(scmMessageSuffix)
        }

        flow.featurePublish(featureName).setPush(true).call();
        flow.git().close()
    }
}
