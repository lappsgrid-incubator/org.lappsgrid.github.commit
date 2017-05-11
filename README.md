# GitHub Commit

The GitHub Commit (ghc) program is a command line program used to commit files to a GitHub repository and create pull requests.

The GitHub API makes it easy to create a branch (one POST request) and create a pull request (another POST).  However, adding a file and committing it takes two GET requests, two POST requests, and one PATCH request. To make things even more interesting not all Java HTTP clients support PATCH since it is not one of the standard HTTP/1.1 verbs.

**NOTE** This project currently uses a customized version of the [http-builder-ng](https://github.com/ksuderman/http-builder-ng) module that adds support for the `HTTP PATCH` method.  A pull request has been submitted and hopefully `PATCH` will be included in the next release.

## Usage

```
java -jar ghc.jar -f <configuration file> -t <github token>
```

### Parameters

**-f**  &lt;path&gt; | **--file** &lt;path&gt;<br/>
The path to the *commit data file*. See below for more information.

**-t** &lt;string&gt; | **--token** &lt;string&gt;<br/>
Your GitHub API token. If you do not have a GitHub API token you will need to get one.


## Commit Data File

To commit one or more files to a GitHub repository and create a pull request requires more information than can be comfortably put on the command line.  Therefore one of the the parameters to the *ghc* program is a [Groovy configuration](http://docs.groovy-lang.org/latest/html/gapi/groovy/util/ConfigSlurper.html) file that specifies:

1. The repository owner/organization. [**Optional**]
1. The name of the repository to commit to.
1. The name of the branch to use as the *base* of the pull request. [**Optional**]
1. The name of the branch to create. This is the branch that will be pulled back into the *base* branch [**Optional**]
1. The commit message to use. [**Optional**]
1. The title to use for the pull request. [**Optional**]
1. The body (message) of the pull request. [**Optional**]
1. A list of the files to be committed. For each file you must specify:
    1. the **file** to be committed.
    1. the **path** of the file in the repository. If a file exists at that path it will be replaced, otherwise the file will be created.

If the name of the *owner* is not specified then the *lapps* organization will be assumed.

If the name of the *base* branch is not specified the *develop* branch will be assumed. It is also assumed that there **is** a develop branch!

If the name of the new branch is not specified a branch name will be derived by appending a timestamp to the string *vocabulary-*.  For example, *vocabulary-20170506-121034*.

### Full Example

```groovy
owner = 'lapps'
repository = 'org.lappsgrid.vocabulary'
base = 'develop'
branch = 'vocabulary-update' + new Date().format('yyyyMMdd')
pr {
    title = 'Vocabulary update'
    message = 'The vocabulary pages have been updated. Please see http://vocab.lappsgrid.org.'
}
files = [
    [
        file: 'target/Annotations.java',
        path: 'src/main/java/org/lappsgrid/vocabulary/Annotations.java'
    ],
    [
        file: 'target/Features.java',
        path: 'src/main/java/org/lappsgrid/vocabulary/Features.java'
    ]
]

```

### Minimal Example

```groovy
repository = 'org.lappsgrid.vocabulary'
files = [
    [
        file: 'target/Annotations.java',
        path: 'src/main/java/org/lappsgrid/vocabulary/Annotations.java'
    ],
    [
        file: 'target/Features.java',
        path: 'src/main/java/org/lappsgrid/vocabulary/Features.java'
    ]
]

```
