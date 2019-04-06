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
package io.github.robwin.jgitflow.tasks.credentialsprovider

import com.atlassian.jgitflow.core.JGitFlowReporter
import com.google.common.base.Strings
import com.jcraft.jsch.JSch
import com.jcraft.jsch.JSchException
import com.jcraft.jsch.Session
import org.eclipse.jgit.transport.CredentialsProvider
import org.eclipse.jgit.transport.JschConfigSessionFactory
import org.eclipse.jgit.transport.OpenSshConfig
import org.eclipse.jgit.transport.SshSessionFactory
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.eclipse.jgit.util.FS
import org.gradle.api.Project

class CredentialsProviderHelper {
    public static void setupCredentialProvider(Project project) {

        /*
            Configure Basic SSH: (Ref: https://www.codeaffine.com/2014/12/09/jgit-authentication/)
            JSchConfigSessionFactory is mostly compatible with OpenSSH, the SSH implementation used by native Git.
            It loads the known hosts and private keys from their default locations (identity, id_rsa and id_dsa) in the
            user’s .ssh directory.
            If the ssh connection is using public/private key authentication with no passphrase then the user does not
            need to configure anything.
            If the ssh connection is using public/private key authentication with different named key files or
            using a passphrase then the user can configure gitPrvKeyPath and gitPrvKeyPassPhrase.
            If the ssh connection is not using public/private key authentication, then the user can configure
            the gitPassword property to use for ssh access.
         */
        SshSessionFactory.setInstance( new JschConfigSessionFactory() {
            @Override
            protected void configure( OpenSshConfig.Host host, Session session ) {

                // The following is used for SSH if not using public/private keys
                if( project.hasProperty('gitPassword') ) {
                    String password = project.property("gitPassword")
                    if( !Strings.isNullOrEmpty( password ) ) {
                        session.setPassword( password );
                    }
                }
            }

            @Override
            protected JSch createDefaultJSch( FS fs ) throws JSchException {
                JSch defaultJSch = super.createDefaultJSch( fs );
                if( project.hasProperty('gitPrvKeyPath') ) {
                    String gitPrvKeyPath = project.property("gitPrvKeyPath")
                    if( !Strings.isNullOrEmpty( gitPrvKeyPath ) ) {
                        // Optional Private Key Passphrase
                        String gitPrvKeyPassPhrase = null;
                        if( project.hasProperty('gitPrvKeyPassPhrase') ) {
                            gitPrvKeyPassPhrase = project.property( "gitPrvKeyPassPhrase" )
                            if( !Strings.isNullOrEmpty( gitPrvKeyPassPhrase ) ) {
                                gitPrvKeyPassPhrase = null;
                            }
                        }
                        else if( project.hasProperty('gitPrvKeyPassPhraseFilename') ) {
                            String gitPrvKeyPassPhraseFilename = project.property( "gitPrvKeyPassPhraseFilename" )
                            if( !Strings.isNullOrEmpty( gitPrvKeyPassPhraseFilename ) ) {
                                 gitPrvKeyPassPhrase = new File( gitPrvKeyPassPhraseFilename ).getText('UTF-8' );
                            }
                        }
                        defaultJSch.addIdentity( gitPrvKeyPath, gitPrvKeyPassPhrase );
                    }
                }
                return defaultJSch;
            }
        } );


        if (project.hasProperty('gitUsername') && project.hasProperty('gitPassword')) {
            String username = project.property("gitUsername")
            String password = project.property("gitPassword")
            if (!Strings.isNullOrEmpty(username) && !Strings.isNullOrEmpty(password)) {
                JGitFlowReporter.get().debugText(getClass().getSimpleName(), "using provided username and password");
                CredentialsProvider.setDefault(new UsernamePasswordCredentialsProvider(username, password));
            }
        }
    }
}