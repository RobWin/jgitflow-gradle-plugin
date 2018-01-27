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
package io.github.robwin.jgitflow.tasks.helper;

import com.github.zafarkhaja.semver.Version;
import org.gradle.api.GradleException;

import java.util.regex.Pattern;

public class ArtifactHelper {

    private static final Pattern VERSION_FILE_PATTERN = Pattern.compile("^(.*)-([0-9]{8}.[0-9]{6})-([0-9]+)$");
    private static final String SNAPSHOT_VERSION = "SNAPSHOT";

    public static boolean isSnapshot(String version) {
        if(version != null) {
            if(version.regionMatches(true, version.length() - SNAPSHOT_VERSION.length(), SNAPSHOT_VERSION, 0, SNAPSHOT_VERSION.length())) {
                return true;
            }

            if(VERSION_FILE_PATTERN.matcher(version).matches()) {
                return true;
            }
        }

        return false;
    }

    public static String removeSnapshot(String version) {
        return Version.valueOf(version).getNormalVersion();
    }

    public static String newSnapshotVersion(String releaseVersion, String newVersionIncrement) {

        switch (newVersionIncrement.toUpperCase()) {
            case "PATCH":
                return Version.valueOf(releaseVersion)
                        .incrementPatchVersion()
                        .setPreReleaseVersion(SNAPSHOT_VERSION)
                        .toString();
            case "MINOR":
                return Version.valueOf(releaseVersion)
                        .incrementMinorVersion()
                        .setPreReleaseVersion(SNAPSHOT_VERSION)
                        .toString();
            case "MAJOR":
                return Version.valueOf(releaseVersion)
                        .incrementMajorVersion()
                        .setPreReleaseVersion(SNAPSHOT_VERSION)
                        .toString();
            default:
                throw new GradleException("Invalid value '" + newVersionIncrement + "' for newVersionIncrement Property");

        }
    }

}
