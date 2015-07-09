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

import io.github.robwin.jgitflow.tasks.FeatureFinishTask
import io.github.robwin.jgitflow.tasks.FeatureStartTask
import io.github.robwin.jgitflow.tasks.HotfixFinishTask
import io.github.robwin.jgitflow.tasks.HotfixStartTask
import io.github.robwin.jgitflow.tasks.ReleaseFinishTask
import io.github.robwin.jgitflow.tasks.ReleaseStartTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class JGitflowPlugin implements Plugin<Project> {

    static final String RELEASE_START_TASK_NAME = 'releaseStart'
    static final String RELEASE_FINISH_TASK_NAME = 'releaseFinish'
    static final String FEATURE_START_TASK_NAME = 'featureStart'
    static final String FEATURE_FINISH_TASK_NAME = 'featureFinish'
    static final String HOTFIX_START_TASK_NAME = 'hotfixStart'
    static final String HOTFIX_FINISH_TASK_NAME = 'hotfixFinish'
    static final String GROUP_NAME = 'jgitflow'

    @Override
    void apply(Project project) {
        project.task(
                RELEASE_START_TASK_NAME,
                type: ReleaseStartTask,
                group: GROUP_NAME,
                description: 'Prepares the project for a release. Creates a release branch and updates gradle with the release version.')

        project.task(
                RELEASE_FINISH_TASK_NAME,
                type: ReleaseFinishTask,
                group: GROUP_NAME,
                description: 'Releases the project. Merges the release branch and optionally pushes changes and updates gradle to new development version.')


        project.task(
                FEATURE_START_TASK_NAME,
                type: FeatureStartTask,
                description: 'Prepares the project for a new feature. Creates a feature branch and updates gradle with the feature version.',
                group: GROUP_NAME){
        }

        project.task(
                FEATURE_FINISH_TASK_NAME,
                type: FeatureFinishTask,
                description: 'Finishes the feature. Merges the feature branch and updates gradle to previous develop version.',
                group: GROUP_NAME){
        }

        project.task(
                HOTFIX_START_TASK_NAME,
                type: HotfixStartTask,
                description: 'Prepares the project for a hotfix. Creates a hotfix branch and updates gradle with the hotfix version.',
                group: GROUP_NAME){
        }

        project.task(
                HOTFIX_FINISH_TASK_NAME,
                type: HotfixFinishTask,
                description: 'Releases the project. Merges the hotfix branch and optionally pushes changes and updates gradle to previous version.',
                group: GROUP_NAME) {
        }
    }

}
