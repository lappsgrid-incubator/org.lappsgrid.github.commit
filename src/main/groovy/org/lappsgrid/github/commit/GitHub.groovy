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
//    String token
//    String owner
//    String repo

    public GitHub(String owner, String repo, String token) {
        parser = new JsonSlurper();
        github = new HttpUtils('https://api.github.com', token)
                .repo(repo)
                .owner(owner)
    }

    public Map branch(String base) {
        String timestamp = new Date().format('yyyyMMdd-HHmmss')
        String name = 'vocabulary-' + timestamp
        return branch(base, name)
    }

    public Map branch(String base, String name) {
        Map head = github.get('git/refs/heads/' + base)

        // ref is the branch we want to create.
        // sha is the SHA of the branch we are branching from
        Map data = [
             ref: "refs/heads/${name}".toString(),
             sha: head.object.sha
        ]
        Map branch = github.post('git/refs', data)
        return branch
    }

    public Map pullRequest(String head) {
        return pullRequest(head, 'master')
    }

    public Map pullRequest(String head, String base) {
        Map data = [
                title: 'Vocabulary Update',
                head: head,
                base: base,
                body: 'The vocabulary has been updated. See http://vocab.lappsgrid.org',
                maintainer_can_modify: true
        ]
        return github.post('pulls', data)
    }

    public Map commit(String branch, List<Map> data) {
        /*
         * Step 1. Get a reference to the HEAD of the branch.
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
         * Step 3. POST the file(s) to GitHub.
         */
        Map entity = [
                base_tree: commit.tree.sha,
                tree: []
        ]
        data.each { entity.tree << send(it) }
        Map newTree = github.post("git/trees", entity)


        /*
         * Step 4. Create a new commit containing the tree we just created.
         */
        println "Creating a new commit for ${head.object.sha}"
        Map newCommit = [:]
        newCommit.message = 'New Discriminators after vocabulary build.'
        newCommit.parents = [ head.object.sha ]
        newCommit.tree = newTree.sha

//        data = [
//                message: 'New Discriminators after vocabulary build.',
//                parents: [ head.object.sha ],
//                tree: newTree.sha
//        ]
        println "Attempting the POST"
        def newCommitResult = github.post("git/commits", newCommit)
        prettyPrint newCommitResult

        /*
         * Step 6. Update HEAD to point to the new commit
         */
        println "Updating HEAD"
        Map updateHead = [
                sha: newCommitResult.sha,
                force: true
        ]
        def update = github.post("git/refs/heads/$branch", updateHead)

        println "Commit complete."
        return update
    }

    /**
     * Sends a single file to the GitHub repository.
     *
     * @param commitData a map of containing the file and path
     * @return The tree fragment required when we create a new tree for this file.
     */
    Map send(Map commitData) {
        File file = new File(commitData.file)
        Map data = [
                content: file.text.bytes.encodeBase64().toString(),
                encoding: 'base64'
        ]
        Map blob = github.post('git/blobs', data)
        return [
                path: commitData.path,
                mode: '100644',
                type: 'blob',
                sha: blob.sha
        ]
    }

    void prettyPrint(Object object) {
        println new JsonBuilder(object).toPrettyString()
    }

}
