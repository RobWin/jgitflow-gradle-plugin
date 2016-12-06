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
package io.github.robwin.jgitflow.tasks.helper

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.LogCommand
import org.eclipse.jgit.api.Status
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.StoredConfig
import org.eclipse.jgit.revwalk.RevCommit
import org.gradle.api.GradleException
import org.gradle.api.Project

import java.nio.file.Files
import java.security.SecureRandom

class GitHelper {

    private static SecureRandom random = new SecureRandom()

    public static void add(Git git, String filePattern) {
        git.add().addFilepattern(filePattern).call();
    }

    public static void addRemote(Git git, String remoteName, String remoteUri) {
        StoredConfig config = git.getRepository().getConfig();
        config.setString("remote", remoteName, "url", remoteUri);
        config.setString("remote", remoteName, "fetch", "+refs/heads/*:refs/remotes/origin/*")
        config.save();
    }

    public static Git createTempRepository(String repoName, boolean bare) {
        File repoDir = Files.createTempDirectory(repoName).toFile()
        return Git.init().setDirectory(repoDir).setBare(bare).call()
    }

    public static void createBranch(Git git, String name) {
        git.branchCreate().setName(name).call()
    }

    public static Ref findBranch(Git git, String name) {
        return branchList(git).find { ref -> ref.name == "refs/heads/${name}"}
    }

    public static List<Ref> branchList(Git git) {
        return git.branchList().call()
    }

    public static void checkout(Git git, String name) {
        git.checkout().setName(name).call()
    }

    public static RevCommit randomCommit(Git git, String fileName = '1.txt', String message = "Commited file '${fileName}'.") {
        byte[] bytes = new byte[128]
        random.nextBytes(bytes)
        new File(git.repository.directory.parentFile, fileName) << bytes
        add(git, fileName)
        return commit(git, message)
    }

    public static RevCommit commit(Git git, String message) {
        return git.commit().setMessage(message).call()
    }

    public static List<RevCommit> log(Git git, String branch = null, int maxCount = -1) {
        LogCommand logCommand = git.log().setMaxCount(maxCount);
        if (branch != null) {
            ObjectId branchHead = git.repository.resolve("refs/heads/${branch}")
            logCommand.add(branchHead);
        }
        return logCommand.call().asList();
    }

    public static void push(Git git) {
        git.push().call();
    }

    public static void updateGradlePropertiesFile(Project project, String version)
    {
        String currentVersion = project.version
        File propertiesFile = project.file(Project.GRADLE_PROPERTIES)
        if (!propertiesFile.file) {
            propertiesFile.append("version=${version}")
        }else {
            project.ant.replace(file: propertiesFile, token: "version=${currentVersion}", value: "version=${version}", failOnNoReplacements: true)
        }
    }

    public static void commitGradlePropertiesFile(Git git, String message) {
        try {
            Status status = git.status().call()
            if (!status.isClean()) {
                git.add().addFilepattern(".").call()
                git.commit().setMessage(message).call()
            }
        }catch (GitAPIException e) {
            throw new GradleException("Failed to commit gradle.properties: ${e.message}", e)
        }
    }
}