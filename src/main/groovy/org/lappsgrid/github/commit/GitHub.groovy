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

    public GitHub(String owner, String repo, String token) {
        parser = new JsonSlurper();
        github = new HttpUtils('https://api.github.com', token)
                .repo(repo)
                .owner(owner)
    }

    /**
     * Create a new branch in the repository. The name of the new branch will be automatically
     * generated.
     *
     * @param base the branch to base the new branch on.
     * @return the JSON data structure returned by GitHub.
     */
    public Map branch(String base) {
        String timestamp = new Date().format('yyyyMMdd-HHmmss')
        String name = 'vocabulary-' + timestamp
        return branch(base, name)
    }

    /**
     * Create a new branch in the repository.
     *
     * @param base the branch to use as the base of the new branch.
     * @param name the name of the new branch.
     * @return the JSON data structure returned by GitHub.
     */
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

    public Map pullRequest(String head, String title, String body) {
        return pullRequest(head, 'master', title, body)
    }

    public Map pullRequest(String head, String base, String title, String body) {
        Map data = [
                title: title,
                head: head,
                base: base,
                body: body,
                maintainer_can_modify: true
        ]
        return github.post('pulls', data)
    }

    public Map commit(String branch, List<Map> data, String message) {
        /*
         * Step 1. Get a reference to the HEAD of the branch.
         */
        println "Getting pointer to HEAD"
        def head = github.get("git/refs/heads/$branch".toString())
        //prettyPrint head

        /*
         * Step 2. Get the commit that HEAD points to.
         */
        println "Getting commit object"
        def commit = github.get(head.object.url)
        //prettyPrint commit

        /*
         * Step 3. POST the file(s) to GitHub and create a new tree.
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
        newCommit.message = message
        newCommit.parents = [ head.object.sha ]
        newCommit.tree = newTree.sha

        def newCommitResult = github.post("git/commits", newCommit)
        //prettyPrint newCommitResult

        /*
         * Step 5. Update HEAD to point to the new commit
         */
        println "Updating HEAD"
        Map updateHead = [
                sha: newCommitResult.sha,
                force: true
        ]
        def update = github.patch("git/refs/heads/$branch", updateHead)

        println "Commit complete."
        prettyPrint(update)
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
        String encoded
        if (commitData.binary) {
            encoded = file.bytes.encodeBase64().toString()
        }
        else {
            encoded = file.text.bytes.encodeBase64().toString()
        }
        Map data = [
                content: encoded,
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
