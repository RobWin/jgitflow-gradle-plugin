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

import com.atlassian.jgitflow.core.InitContext
import com.atlassian.jgitflow.core.JGitFlow
import io.github.robwin.jgitflow.tasks.credentialsprovider.CredentialsProviderHelper
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

class InitJGitflowTask extends DefaultTask {

    @Input
    @Optional
    String master = "master";

    @Input
    @Optional
    String develop = "develop";

    @Input
    @Optional
    String release = "release/";

    @Input
    @Optional
    String feature = "feature/";

    @Input
    @Optional
    String hotfix = "hotfix/";

    @Input
    @Optional
    String versiontag = "";


    @TaskAction
    void init(){
        CredentialsProviderHelper.setupCredentialProvider(project)
        InitContext initContext = new InitContext()
        initContext.setMaster(master)
                .setDevelop(develop)
                .setRelease(release)
                .setFeature(feature)
                .setHotfix(hotfix)
                .setVersiontag(versiontag)
        JGitFlow flow = JGitFlow.forceInit(project.rootProject.rootDir, initContext)

        //Switch to develop branch
        flow.git().checkout().setName(flow.getDevelopBranchName()).call()

        flow.git().close()
    }

}
