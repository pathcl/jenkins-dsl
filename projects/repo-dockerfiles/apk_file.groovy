freeStyleJob('apk_file') {
    displayName('apk-file')
    description('Build Dockerfiles in genuinetools/apk-file.')

    concurrentBuild()
    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/genuinetools/apk-file')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/apk-file', 'Docker Hub: jess/apk-file', 'notepad.png')
            link('https://r.j3ss.co/repo/apk-file/tags', 'Registry: r.j3ss.co/apk-file', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('https://github.com/genuinetools/apk-file.git')
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
        shell('docker build --rm --force-rm -t r.j3ss.co/apk-file:$(cat .branch) .')
shell('docker tag r.j3ss.co/apk-file:$(cat .branch) jess/apk-file:$(cat .branch)')
        shell('docker push --disable-content-trust=false r.j3ss.co/apk-file:$(cat .branch)')
        shell('docker push --disable-content-trust=false jess/apk-file:$(cat .branch)')
        shell('if [[ "$(cat .branch)" != "latest" ]]; then docker tag r.j3ss.co/apk-file:$(cat .branch) r.j3ss.co/apk-file:latest; docker push --disable-content-trust=false r.j3ss.co/apk-file:latest; fi')
        shell('if [[ "$(cat .branch)" != "latest" ]]; then docker tag jess/apk-file:$(cat .branch) jess/apk-file:latest; docker push --disable-content-trust=false jess/apk-file:latest; fi')
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
