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
import org.gradle.api.Plugin
import org.gradle.api.Project

class JGitflowPlugin implements Plugin<Project> {

    static final String RELEASE_START_TASK_NAME = 'releaseStart'

    static final String RELEASE_FINISH_TASK_NAME = 'releaseFinish'

    @Override
    void apply(Project project) {
        project.task(RELEASE_START_TASK_NAME, type: ReleaseStartTask, group: 'jgitflow',
                description: 'Prepares the project for a release. Creates a release branch and updates gradle with the release version.')

        project.task(RELEASE_FINISH_TASK_NAME, type: ReleaseFinishTask, group: 'jgitflow',
                description: 'Releases the project. Builds the project, Merges the release branch (as per git-flow), optionally pushes changes and updates gradle to new development version.')
    }

}
