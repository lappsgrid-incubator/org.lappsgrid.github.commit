package org.lappsgrid.github.commit

/**
 * @author Keith Suderman
 */
class Main {

    void run(File file, String token) {
        def config = new ConfigSlurper().parse(file.text)
        GitHub github = new GitHub(config.owner, config.repository, token)
        String base = config.base ?: 'develop'
        String branch = config.branch ?: 'vocabulary-' + new Date().format('yyyyMMdd-HHmmss')
        github.branch(base, branch)
        github.commit(branch, config.files)
        github.pullRequest(branch, config.base)

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
