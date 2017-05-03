package org.lappsgrid.github.commit

import groovy.json.JsonSlurper
import groovyx.net.http.HttpBuilder

/**
 * The HttpUtils class simplifies using the HttpBuilder class with the GitHub API. In
 * particular the HttpUtils class takes care of setting the Authorization and
 * contentType headers as well as eliminating some of the redundancies in the
 * GitHub urls.
 *
 * @author Keith Suderman
 */
class HttpUtils {

    protected HttpBuilder http
    protected JsonSlurper parser
    protected String owner
    protected String repo

    HttpUtils(String token) {
        this('https://api.github.com', token)
    }

    HttpUtils(String uri, String token) {
        parser = new JsonSlurper()
        http = HttpBuilder.configure {
            request.uri = uri
            request.headers.Authorization = "token $token".toString()
        }
    }

    HttpUtils owner(String owner) {
        this.owner = owner
        return this
    }
    HttpUtils repo(String repo) {
        this.repo = repo
        return this
    }

    Map post(String path, Map data) {
        return http.post(Map) {
            request.uri.path = makePath(path)
            request.contentType = 'application/json'
            request.body = data
        }
    }

    def get(String path) {
        if (path.startsWith('http')) {
            String json = new URL(path).text
            return parser.parseText(json)
        }

        return http.get {
            request.uri.path = makePath(path)

            response.failure {
                println "Failed to get $path"
            }
        }
    }

    String makePath(String path) {
        if (path.startsWith('/')) {
            path = path.substring(1)
        }
        return "/repos/$owner/$repo/$path".toString()
    }

}
