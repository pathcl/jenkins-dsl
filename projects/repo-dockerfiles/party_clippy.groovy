freeStyleJob('party_clippy') {
    displayName('party-clippy')
    description('Build Dockerfiles in jessfraz/party-clippy.')

    concurrentBuild()
    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/party-clippy')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/party-clippy', 'Docker Hub: jess/party-clippy', 'notepad.png')
            link('https://r.j3ss.co/repo/party-clippy/tags', 'Registry: r.j3ss.co/party-clippy', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('https://github.com/jessfraz/party-clippy.git')
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
        shell('docker build --rm --force-rm -t r.j3ss.co/party-clippy:$(cat .branch) .')
shell('docker tag r.j3ss.co/party-clippy:$(cat .branch) jess/party-clippy:$(cat .branch)')
        shell('docker push --disable-content-trust=false r.j3ss.co/party-clippy:$(cat .branch)')
        shell('docker push --disable-content-trust=false jess/party-clippy:$(cat .branch)')
        shell('if [[ "$(cat .branch)" != "latest" ]]; then docker tag r.j3ss.co/party-clippy:$(cat .branch) r.j3ss.co/party-clippy:latest; docker push --disable-content-trust=false r.j3ss.co/party-clippy:latest; fi')
        shell('if [[ "$(cat .branch)" != "latest" ]]; then docker tag jess/party-clippy:$(cat .branch) jess/party-clippy:latest; docker push --disable-content-trust=false jess/party-clippy:latest; fi')
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
