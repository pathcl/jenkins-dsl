freeStyleJob('audit') {
    displayName('audit')
    description('Build Dockerfiles in genuinetools/audit.')

    concurrentBuild()
    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/genuinetools/audit')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/audit', 'Docker Hub: jess/audit', 'notepad.png')
            link('https://r.j3ss.co/repo/audit/tags', 'Registry: r.j3ss.co/audit', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('https://github.com/genuinetools/audit.git')
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
        shell('docker build --rm --force-rm -t r.j3ss.co/audit:$(cat .branch) .')
shell('docker tag r.j3ss.co/audit:$(cat .branch) jess/audit:$(cat .branch)')
        shell('docker push --disable-content-trust=false r.j3ss.co/audit:$(cat .branch)')
        shell('docker push --disable-content-trust=false jess/audit:$(cat .branch)')
        shell('if [[ "$(cat .branch)" != "latest" ]]; then docker tag r.j3ss.co/audit:$(cat .branch) r.j3ss.co/audit:latest; docker push --disable-content-trust=false r.j3ss.co/audit:latest; fi')
        shell('if [[ "$(cat .branch)" != "latest" ]]; then docker tag jess/audit:$(cat .branch) jess/audit:latest; docker push --disable-content-trust=false jess/audit:latest; fi')
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
