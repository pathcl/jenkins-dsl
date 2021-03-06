freeStyleJob('mirror_udict') {
    displayName('mirror-udict')
    description('Mirror github.com/genuinetools/udict to g.j3ss.co/genuinetools/udict.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/genuinetools/udict')
        sidebarLinks {
            link('https://git.j3ss.co/genuinetools/udict', 'git.j3ss.co/genuinetools/udict', 'notepad.png')
        }
    }
    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }
    triggers {
        cron('H H * * *')
    }
    wrappers { colorizeOutput() }
    steps {
        shell('git clone --mirror https://github.com/genuinetools/udict.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/genuinetools/udict.git')
    }
    publishers {
        extendedEmail {
            recipientList('$DEFAULT_RECIPIENTS')
            contentType('text/plain')
            triggers {
                stillFailing {
                    attachBuildLog(true)
                }
            }
        }
        wsCleanup()
    }
}
