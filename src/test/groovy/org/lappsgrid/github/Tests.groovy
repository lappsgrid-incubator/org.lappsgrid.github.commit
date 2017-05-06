package org.lappsgrid.github

import groovy.json.*
import groovyx.net.http.HttpBuilder
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.lappsgrid.github.commit.GitHub
import org.lappsgrid.github.commit.HttpUtils

/**
 * @author Keith Suderman
 */
@Ignore
class Tests {

    protected HttpUtils http
    protected GitHub hub
    String token

    @Before
    void setup() {
        token = new File('/Users/suderman/.secret/github.token').text
        hub = new GitHub('lapps', 'org.lappsgrid.discriminator', token)
        http = new HttpUtils('https://api.github.com', token)
        http.owner('lapps')
        http.repo('org.lappsgrid.discriminator')
    }

    @After
    void teardown() {
        token = null
        hub = null
        http = null
    }

    @Test
    void fixit() {
        String sha = "7acb40112ccab583bd606076e71b2bc97d199c1c"
        Map data = [
                sha: sha,
                force: true
        ]
        http.post('git/refs/heads/vocabulary-20170505-002452', data)
    }

    @Test
    void pr() {
        hub.pullRequest('vocabulary-20170505-002452', 'develop')
    }

    void prettyPrint(Object object) {
        println new JsonBuilder(object).toPrettyString()
    }
}
