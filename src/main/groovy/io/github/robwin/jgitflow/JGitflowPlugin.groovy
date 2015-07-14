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
import io.github.robwin.jgitflow.tasks.FeaturePublishTask
import io.github.robwin.jgitflow.tasks.FeatureStartTask
import io.github.robwin.jgitflow.tasks.HotfixFinishTask
import io.github.robwin.jgitflow.tasks.HotfixPublishTask
import io.github.robwin.jgitflow.tasks.HotfixStartTask
import io.github.robwin.jgitflow.tasks.InitJGitflowTask
import io.github.robwin.jgitflow.tasks.ReleaseFinishTask
import io.github.robwin.jgitflow.tasks.ReleasePublishTask
import io.github.robwin.jgitflow.tasks.ReleaseStartTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class JGitflowPlugin implements Plugin<Project> {

    static final String INIT_JGTIFLOW_TASK_NAME = 'initJGitflow'
    static final String RELEASE_START_TASK_NAME = 'releaseStart'
    static final String RELEASE_FINISH_TASK_NAME = 'releaseFinish'
    static final String RELEASE_PUBLISH_TASK_NAME = 'releasePublish'
    static final String FEATURE_START_TASK_NAME = 'featureStart'
    static final String FEATURE_FINISH_TASK_NAME = 'featureFinish'
    static final String FEATURE_PUBLISH_TASK_NAME = 'featurePublish'
    static final String HOTFIX_START_TASK_NAME = 'hotfixStart'
    static final String HOTFIX_FINISH_TASK_NAME = 'hotfixFinish'
    static final String HOTFIX_PUBLISH_TASK_NAME = 'hotfixPublish'
    static final String GROUP_NAME = 'jgitflow'

    @Override
    void apply(Project project) {
        project.task(
                INIT_JGTIFLOW_TASK_NAME,
                type: InitJGitflowTask,
                group: GROUP_NAME,
                description: 'Initializes the JGitflow context.')

        project.task(
                RELEASE_START_TASK_NAME,
                type: ReleaseStartTask,
                group: GROUP_NAME,
                description: 'Creates a release branch and updates gradle with the release version.')

        project.task(
                RELEASE_FINISH_TASK_NAME,
                type: ReleaseFinishTask,
                group: GROUP_NAME,
                description: 'Merges a release branch back into the master branch and develop branch and pushes everything to the remote origin.')

        project.task(
                RELEASE_PUBLISH_TASK_NAME,
                type: ReleasePublishTask,
                group: GROUP_NAME,
                description: 'Publishes a release branch to the remote origin.')

        project.task(
                FEATURE_START_TASK_NAME,
                type: FeatureStartTask,
                description: 'Creates a feature branch.',
                group: GROUP_NAME){
        }

        project.task(
                FEATURE_FINISH_TASK_NAME,
                type: FeatureFinishTask,
                description: 'Merges a feature branch back into the develop branch.',
                group: GROUP_NAME){
        }

        project.task(
                FEATURE_PUBLISH_TASK_NAME,
                type: FeaturePublishTask,
                group: GROUP_NAME,
                description: 'Publishes a feature branch to the remote origin.')

        project.task(
                HOTFIX_START_TASK_NAME,
                type: HotfixStartTask,
                description: 'Creates a hotfix branch.',
                group: GROUP_NAME){
        }

        project.task(
                HOTFIX_FINISH_TASK_NAME,
                type: HotfixFinishTask,
                description: ' Merges a hotfix branch back into the master branch and develop branch.',
                group: GROUP_NAME) {
        }

        project.task(
                HOTFIX_PUBLISH_TASK_NAME,
                type: HotfixPublishTask,
                group: GROUP_NAME,
                description: 'Publishes a hotfix branch to the remote origin.')
    }

}
