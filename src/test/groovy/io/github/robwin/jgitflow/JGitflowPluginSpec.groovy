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

import io.github.robwin.jgitflow.tasks.ReleaseFinishTask
import io.github.robwin.jgitflow.tasks.ReleaseStartTask
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class JGitflowPluginSpec extends Specification{

    Project project

    def setup(){
        project = ProjectBuilder.builder().build()
    }

    def "JGitflowPlugin should be applied to project with default setup"() {
        expect:
            project.tasks.findByName(JGitflowPlugin.RELEASE_START_TASK_NAME) == null
            project.tasks.findByName(JGitflowPlugin.RELEASE_FINISH_TASK_NAME) == null
        when:
            project.pluginManager.apply 'io.github.robwin.jgitflow'
        then:
            ReleaseStartTask releaseStartTask = (ReleaseStartTask) project.tasks.findByName(JGitflowPlugin.RELEASE_START_TASK_NAME)
            releaseStartTask != null
            releaseStartTask.group == 'jgitflow'
            ReleaseFinishTask releaseFinishTask = (ReleaseFinishTask) project.tasks.findByName(JGitflowPlugin.RELEASE_FINISH_TASK_NAME)
            releaseFinishTask != null
            releaseFinishTask.group == 'jgitflow'
    }
}
