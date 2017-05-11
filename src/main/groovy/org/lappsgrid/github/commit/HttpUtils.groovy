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

import groovy.json.JsonSlurper
import groovyx.net.http.ApacheHttpBuilder
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
//        def factory = { new ApacheHttpBuilder(it) }
        http = HttpBuilder.configure({ new ApacheHttpBuilder(it) }) {

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

    Map patch(String path, Map data) {
        return http.patch(Map) {
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
