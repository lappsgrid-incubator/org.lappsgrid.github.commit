package org.lappsgrid.github.commit

/**
 * @author Keith Suderman
 */
class Main {

    static void main(String[] args) {
        CliBuilder cli = new CliBuilder()
        cli.header = "Commits a single file to a GitHub repository.\n"
        cli.footer = "\nCopyright 2017 The Language Applications Grid\n"
        cli.f(longOpt:'file', args:1, "the file to be commited to GitHub.")
        cli.o(longOpt:'owner', args:1, "the owner/organization of the repository")
        cli.r(longOpt:'repo', args:1, "the repository name")
        cli.b(longOpt:'branch', args:1, "the base branch")
        cli.p(longOpt:'path', args:1, "the full repository path of the file to write")
        cli.t(longOpt:'token', args:1, "your GitHub API token")
        cli.v(longOpt:'version', "prints the program version number")
        cli.h(longOpt:'help', "prints this help message")


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
            println "Specified file not found."
            return
        }

        if (!params.r) {
            println "No repository specified."
            cli.usage()
            return
        }
        if (!params.b) {
            println "No branch specified."
            cli.usage()
            return
        }
        if (!params.p) {
            println "No GitHub path specified."
            cli.usage()
            return
        }

        String owner = 'lapps'
        if (params.o) {
            owner = params.o
        }
        else {
            println "No owner specified. Using 'lapps' as the default."
        }
        GitHub github = new GitHub(owner, params.r, params.t)
        Map branch = github.branch(params.b)
        String branchName = branch.ref.tokenize('/')[-1]
        println "Created branch ${branchName}"
        Map commit = github.commit(file, params.p, branchName)
        Map pr = github.pullRequest(branchName)

        println "Pull request submitted."
    }
}
