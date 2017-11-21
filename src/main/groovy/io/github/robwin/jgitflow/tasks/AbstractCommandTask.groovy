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
import com.atlassian.jgitflow.core.command.JGitFlowCommand
import org.codehaus.groovy.util.StringUtil
import org.gradle.api.DefaultTask

class AbstractCommandTask extends DefaultTask {

	static final String BLANK = ""
	static final String SPACE = " "

	void setCommandPrefixAndSuffix(JGitFlowCommand command) {
		// Set scmMessagePrefix if present
		if (project.hasProperty('scmMessagePrefix')) {
			command.setScmMessagePrefix(project.property('scmMessagePrefix').toString().trim() + SPACE)
		}

		// Set scmMessageSuffix if present
		if (project.hasProperty('scmMessageSuffix')) {
			command.setScmMessageSuffix(SPACE + project.property('scmMessageSuffix').toString().trim())
		}
	}

	def getScmMessagePrefix(JGitFlowCommand command) {
		if (command.getScmMessagePrefix()?.trim()) {
			return command.getScmMessagePrefix()
		} else {
			return BLANK
		}
	}

	def getScmMessageSuffix(JGitFlowCommand command) {
		if (command.getScmMessageSuffix()?.trim()) {
			return command.getScmMessageSuffix()
		} else {
			return BLANK
		}
	}
}
