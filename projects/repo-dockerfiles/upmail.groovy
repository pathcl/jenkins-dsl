freeStyleJob('upmail') {
    displayName('upmail')
    description('Build Dockerfiles in genuinetools/upmail.')

    concurrentBuild()
    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/genuinetools/upmail')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/upmail', 'Docker Hub: jess/upmail', 'notepad.png')
            link('https://r.j3ss.co/repo/upmail/tags', 'Registry: r.j3ss.co/upmail', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('https://github.com/genuinetools/upmail.git')
            }
branches('*/master', '*/tags/*')
            extensions {
                wipeOutWorkspace()
                cleanAfterCheckout()
            }
        }
    }

    triggers {
        cron('H H * * *')
        githubPush()
    }

    wrappers { colorizeOutput() }

    environmentVariables(DOCKER_CONTENT_TRUST: '1')
    steps {
        shell('export BRANCH=$(git symbolic-ref -q --short HEAD || git describe --tags --exact-match || echo "master"); if [[ "$BRANCH" == "master" ]]; then export BRANCH="latest"; fi; echo "$BRANCH" > .branch')
        shell('docker build --rm --force-rm -t r.j3ss.co/upmail:$(cat .branch) .')
shell('docker tag r.j3ss.co/upmail:$(cat .branch) jess/upmail:$(cat .branch)')
        shell('docker push --disable-content-trust=false r.j3ss.co/upmail:$(cat .branch)')
        shell('docker push --disable-content-trust=false jess/upmail:$(cat .branch)')
        shell('if [[ "$(cat .branch)" != "latest" ]]; then docker tag r.j3ss.co/upmail:$(cat .branch) r.j3ss.co/upmail:latest; docker push --disable-content-trust=false r.j3ss.co/upmail:latest; fi')
        shell('if [[ "$(cat .branch)" != "latest" ]]; then docker tag jess/upmail:$(cat .branch) jess/upmail:latest; docker push --disable-content-trust=false jess/upmail:latest; fi')
        shell('docker rm $(docker ps --filter status=exited -q 2>/dev/null) 2> /dev/null || true')
        shell('docker rmi $(docker images --filter dangling=true -q 2>/dev/null) 2> /dev/null || true')
    }

    publishers {
        retryBuild {
            retryLimit(2)
            fixedDelay(15)
        }

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
