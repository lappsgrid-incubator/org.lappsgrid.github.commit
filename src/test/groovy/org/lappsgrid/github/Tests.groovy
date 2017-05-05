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
//        github = HttpBuilder.configure {
//            request.uri = "https://api.github.com"
//            request.headers.Authorization = "token $token".toString()
//        }
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

    /*
    @Test
    void testApi() {
        def heads = http.get('git/refs/heads')
        prettyPrint(heads)
    }

    @Test
    void testDates() {
        println new Date().format('yyyyMMdd-HHmmss')
    }

    @Test
    void branch() {
        def branch = hub.branch('master')
        prettyPrint(branch)
    }

    @Test
    void commit() {
        File file = new File('/Users/suderman/Workspaces/IntelliJ/Lappsgrid/vocab/target/Annotations.java')
        def commit = hub.commit(file, 'src/main/java/org/lappsgrid/vocabulary/Annotations.java', 'vocabulary-20170503-001544')
        prettyPrint(commit)
    }

    @Test
    void pullRequest() {
        def pr = hub.pullRequest('vocabulary-20170503-000427')
        prettyPrint(pr)
    }

    @Test
    void split() {
        String s = "refs/heads/vocabulary-20170502-231812"
        println s.tokenize('/')[-1]
    }

    @Test
    void configTest() {
        def config = new ConfigSlurper().parse(new File('vocabulary.commit').text)
        config.keySet().each { String owner ->
            config[owner].repo.each { repo, files ->
                files.each { Map map ->
                    println "Posting ${map.file} to ${repo}:${map.path}"
                }
            }
        }
    }
    */

    void prettyPrint(Object object) {
        println new JsonBuilder(object).toPrettyString()
    }
}
