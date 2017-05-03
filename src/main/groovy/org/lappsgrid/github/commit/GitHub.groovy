package org.lappsgrid.github.commit

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

/**
 * Written with the help of:
 * - http://www.levibotelho.com/development/commit-a-file-with-the-github-api
 * - https://mdswanson.com/blog/2011/07/23/digging-around-the-github-api-take-2.html
 *
 * @author Keith Suderman
 */
class GitHub {

    private JsonSlurper parser
    private HttpUtils github
    String token
    String owner
    String repo

    public GitHub(String owner, String repo, String token) {
        parser = new JsonSlurper();
        github = new HttpUtils('https://api.github.com', token)
                .repo(repo)
                .owner(owner)
    }

    public Map branch(String base) {
        Map head = github.get('git/refs/heads/' + base)
        String timestamp = new Date().format('yyyyMMdd-HHmmss')

        // ref is the branch we want to create.
        // sha is the SHA of the branch we are branching from
        Map data = [
             ref: "refs/heads/vocabulary-${timestamp}".toString(),
             sha: head.object.sha
        ]
        Map branch = github.post('git/refs', data)
        return branch
    }

    public Map pullRequest(String head) {
        // title head base body maintainer_can_modify
        Map data = [
                title: 'Vocabulary Update',
                head: head,
                base: 'master',
                body: 'The vocabulary has been updated. See http://vocab.lappsgrid.org',
                maintainer_can_modify: true
        ]
        return github.post('pulls', data)
    }

    public Map commit(File file, String path, String branch) {
        println "Committing ${file.path} to ${branch}:${path}"

        /*
         * Step 1. Get a reference to HEAD
         */
        println "Getting HEAD"
        def head = github.get("git/refs/heads/$branch".toString())
        prettyPrint head

        /*
         * Step 2. Get the commit that HEAD points to.
         */
        println "Getting commit object"
        def commit = github.get(head.object.url)
        prettyPrint commit

        /*
         * Step 3. POST the file to GitHub.
         */
        println "Posting new file to GitHub"

        // The data to be POSTed to GitHub
        String java = file.text
        def data = [
            content: java.bytes.encodeBase64().toString(),
            encoding: 'base64'
        ]

        def blob = github.post("git/blobs", data)
        prettyPrint blob

        /*
         * Step 4. Get the tree the commit points to.
         */
        println "Getting the tree for the latest commit"
        def tree = github.get(commit.tree.url)
        println "Got ${tree.sha}"

        /*
         * Step 5. Create a new tree for our file.
         */
        println "Creating a new tree."
        def entity = [
            base_tree: commit.tree.sha,
            tree: [
                [
                    path: path,
                    mode: '100644',
                    type: 'blob',
                    sha: blob.sha
                ]
            ]
        ]
        //prettyPrint entity

        Map newTree = github.post("git/trees", entity)
        prettyPrint newTree


        /*
         * Step 6. Create a new commit containing the tree we just created.
         */
        data = [
                message: 'New Discriminators after vocabulary build.',
                parents: [ head.object.sha ],
                tree: newTree.sha
        ]

        println "Creating a new commit."
        //prettyPrint data
        def newCommit = github.post("git/commits", data)
        prettyPrint newCommit

        /*
         * Step 7. Update HEAD to point to the new commit
         */
        println "Updating HEAD"
        data = [
                sha: newCommit.sha,
                force: true
        ]
        prettyPrint data
        def update = github.post("git/refs/heads/$branch", data)
        println "Update:"
        prettyPrint update

        println "Commit complete."
        return update
    }


    void prettyPrint(Object object) {
        println new JsonBuilder(object).toPrettyString()
    }

}
