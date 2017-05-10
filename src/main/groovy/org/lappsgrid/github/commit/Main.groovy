/*
 * Copyright (C) 2017 The Language Applications Grid
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.lappsgrid.github.commit

/**
 * @author Keith Suderman
 */
class Main {

    void run(File file, String token) {
        def config = new ConfigSlurper().parse(file.text)
        if (!config.owner) {
            println "The repository owner was not specified."
            return
        }
        if (!config.repository) {
            println "The repository was not specified."
            return
        }
        GitHub github = new GitHub(config.owner, config.repository, token)
        String base = config.base ?: 'develop'
        String branch = config.branch ?: 'vocabulary-' + new Date().format('yyyyMMdd-HHmmss')
        String message = config.message ?: 'New Discriminators after vocabulary build.'
        String title = config?.pr.title ?: 'Vocabulary update'
        String body = config?.pr.message ?: 'New vocabulary pages have been generated. Please see http://vocab.lappsgrid.org'
        github.branch(base, branch)
        github.commit(branch, config.files, message)
        github.pullRequest(branch, base, title, body)

        println "Pull request submitted."
    }

    static void main(String[] args) {
        CliBuilder cli = new CliBuilder()
        cli.header = "Commits a one or more files to a GitHub repository.\n"
        cli.f(longOpt:'file', args:1, "the commit configuration file.")
        cli.t(longOpt:'token', args:1, "your GitHub API token")
        cli.v(longOpt:'version', "prints the program version number")
        cli.h(longOpt:'help', "prints this help message")
        cli.footer = "\nCopyright 2017 The Language Applications Grid\n"

        def params = cli.parse(args)
        if (!params || params.h) {
            cli.usage()
            return
        }

        if (params.v) {
            println()
            println "Lappsgrid GitHub Commit v" + Version.version
            println "Copyright 2017 The Language Applications Grid"
            println()
            return
        }

        if (!params.f) {
            println "No file specified."
            cli.usage()
            return
        }
        File file = new File(params.f)
        if (!file.exists()) {
            println "Specified configuration file not found."
            return
        }

        if (!params.t) {
            println "No GitHub API token was specified."
            cli.usage()
            return
        }

        new Main().run(file, params.t)
    }
}
