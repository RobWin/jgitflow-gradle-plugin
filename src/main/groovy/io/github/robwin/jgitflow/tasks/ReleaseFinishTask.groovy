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

import com.atlassian.jgitflow.core.InitContext
import com.atlassian.jgitflow.core.JGitFlow
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class ReleaseFinishTask extends DefaultTask {

    @Input
    String releaseVersion;

    @Input
    String newVersion;

    ReleaseFinishTask(){
        group = 'release'
        description = 'Releases the project. Builds the project, Merges the release branch (as per git-flow), optionally pushes changes and updates gradle to new development version.'
    }

    @TaskAction
    void finish(){
        InitContext initContext = new InitContext()
        JGitFlow flow = JGitFlow.getOrInit(project.rootProject.rootDir, initContext)
        flow.releaseFinish(releaseVersion).setPush(true).call();

        //Local working copy is now on develop branch
    }
}
