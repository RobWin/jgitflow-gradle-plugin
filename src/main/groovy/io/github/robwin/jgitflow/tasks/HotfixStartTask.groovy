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

class HotfixStartTask extends DefaultTask {

    @TaskAction
    void start(){
        String hotfixName = project.property('hotfixName')
        CredentialsProviderHelper.setupCredentialProvider(project)
        JGitFlow flow = JGitFlow.get(project.rootProject.rootDir)

        //Start a hotfix
        def command = flow.hotfixStart(hotfixName)
        if (project.hasProperty('baseCommit')) {
            String baseCommit = project.property('baseCommit')
            command.setStartCommit(baseCommit)
        }

        // adding scmMessagePrefix into hotfix start task
        String scmMessagePrefix
        if (project.hasProperty('scmMessagePrefix')) {
            scmMessagePrefix = project.property('scmMessagePrefix')
            command.setScmMessagePrefix(scmMessagePrefix)
        }else{
            scmMessagePrefix = "[JGitFlow Gradle Plugin]"
            command.setScmMessagePrefix(scmMessagePrefix)
        }

        // adding scmMessageSuffix into hotfix start task
        String scmMessageSuffix
        if (project.hasProperty('scmMessageSuffix')) {
            scmMessageSuffix = project.property('scmMessageSuffix')
            command.setScmMessageSuffix(scmMessageSuffix)
        }else{
            scmMessageSuffix = "[JGitFlow Gradle Plugin]"
            command.setScmMessageSuffix(scmMessageSuffix)
        }
        command.call()

        flow.git().close()
    }
}
