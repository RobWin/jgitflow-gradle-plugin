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
import io.github.robwin.jgitflow.tasks.ReleaseFinishTask
import io.github.robwin.jgitflow.tasks.ReleasePublishTask
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
            project.tasks.findByName(JGitflowPlugin.RELEASE_PUBLISH_TASK_NAME) == null
            project.tasks.findByName(JGitflowPlugin.FEATURE_START_TASK_NAME) == null
            project.tasks.findByName(JGitflowPlugin.FEATURE_FINISH_TASK_NAME) == null
            project.tasks.findByName(JGitflowPlugin.FEATURE_PUBLISH_TASK_NAME) == null
            project.tasks.findByName(JGitflowPlugin.HOTFIX_START_TASK_NAME) == null
            project.tasks.findByName(JGitflowPlugin.HOTFIX_FINISH_TASK_NAME) == null
            project.tasks.findByName(JGitflowPlugin.HOTFIX_PUBLISH_TASK_NAME) == null
        when:
            project.pluginManager.apply 'io.github.robwin.jgitflow'
        then:
            ReleaseStartTask releaseStartTask = (ReleaseStartTask) project.tasks.findByName(JGitflowPlugin.RELEASE_START_TASK_NAME)
            releaseStartTask != null
            releaseStartTask.group == JGitflowPlugin.GROUP_NAME
            ReleaseFinishTask releaseFinishTask = (ReleaseFinishTask) project.tasks.findByName(JGitflowPlugin.RELEASE_FINISH_TASK_NAME)
            releaseFinishTask != null
            releaseFinishTask.group == JGitflowPlugin.GROUP_NAME
            ReleasePublishTask releasePublishTask = (ReleasePublishTask) project.tasks.findByName(JGitflowPlugin.RELEASE_PUBLISH_TASK_NAME)
            releasePublishTask != null
            releasePublishTask.group == JGitflowPlugin.GROUP_NAME
            FeatureStartTask featureStartTask = (FeatureStartTask) project.tasks.findByName(JGitflowPlugin.FEATURE_START_TASK_NAME)
            featureStartTask != null
            featureStartTask.group == JGitflowPlugin.GROUP_NAME
            FeatureFinishTask featureFinishTask = (FeatureFinishTask) project.tasks.findByName(JGitflowPlugin.FEATURE_FINISH_TASK_NAME)
            featureFinishTask != null
            featureFinishTask.group == JGitflowPlugin.GROUP_NAME
            FeaturePublishTask featurePublishTask = (FeaturePublishTask) project.tasks.findByName(JGitflowPlugin.FEATURE_PUBLISH_TASK_NAME)
            featurePublishTask != null
            featurePublishTask.group == JGitflowPlugin.GROUP_NAME
            HotfixStartTask hotfixStartTask = (HotfixStartTask) project.tasks.findByName(JGitflowPlugin.HOTFIX_START_TASK_NAME)
            hotfixStartTask != null
            hotfixStartTask.group == JGitflowPlugin.GROUP_NAME
            HotfixFinishTask hotfixFinishTask = (HotfixFinishTask) project.tasks.findByName(JGitflowPlugin.HOTFIX_FINISH_TASK_NAME)
            hotfixFinishTask != null
            hotfixFinishTask.group == JGitflowPlugin.GROUP_NAME
            HotfixPublishTask hotfixPublishTask = (HotfixPublishTask) project.tasks.findByName(JGitflowPlugin.HOTFIX_PUBLISH_TASK_NAME)
            hotfixPublishTask != null
            hotfixPublishTask.group == JGitflowPlugin.GROUP_NAME
    }
}
