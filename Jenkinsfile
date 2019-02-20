#!/usr/bin/env groovy

// Global scope required for multi-stage persistence
def buildInfo = Artifactory.newBuildInfo()
def agentSbtVersion = 'sbt_1-2-4'

pipeline {
    libraries {
        lib('jenkins-pipeline-shared')
    }
    environment {
        ORG = "ons"
        SVC_NAME = "es-algo-topx"
        ALGO_NAME = "TopX"
        TRAVIS_CI_URL = "https://travis-ci.com/ONSdigital/$SVC_NAME"
        ALGO_GIT_URL = "https://git.algorithmia.com/git/$ORG/$ALGO_NAME"
        ALGO_ENDPOINT = "algo://$ORG/$ALGO_NAME/$GIT_COMMIT"
    }
    options {
        skipDefaultCheckout()
        buildDiscarder(logRotator(numToKeepStr: '30', artifactNumToKeepStr: '30'))
        timeout(time: 1, unit: 'HOURS')
        ansiColor('xterm')
    }
    agent { label 'download.jenkins.slave' }
    stages {
        stage('Checkout') {
            agent { label 'download.jenkins.slave' }
            steps {
                checkout scm
                script {
                    buildInfo.name = "${SVC_NAME}"
                    buildInfo.number = "${BUILD_NUMBER}"
                    buildInfo.env.collect()
                }
                colourText("info", "BuildInfo: ${buildInfo.name}-${buildInfo.number}")
                stash name: 'Checkout'
            }
        }

        stage('Build') {
            agent { label "build.${agentSbtVersion}" }
            steps {
                unstash name: 'Checkout'
                sh "sbt compile"
            }
            post {
                success {
                    colourText("info", "Stage: ${env.STAGE_NAME} successful!")
                }
                failure {
                    colourText("warn", "Stage: ${env.STAGE_NAME} failed!")
                }
            }
        }

        stage('Validate') {
            agent { label "build.${agentSbtVersion}" }
            steps {
                unstash name: 'Checkout'
                sh 'sbt coverage test coverageReport'
            }
            post {
                always {
                    junit '**/target/test-reports/*.xml'
                    cobertura autoUpdateHealth: false,
                            autoUpdateStability: false,
                            coberturaReportFile: 'target/**/coverage-report/cobertura.xml',
                            conditionalCoverageTargets: '70, 0, 0',
                            failUnhealthy: false,
                            failUnstable: false,
                            lineCoverageTargets: '80, 0, 0',
                            maxNumberOfBuilds: 0,
                            methodCoverageTargets: '80, 0, 0',
                            onlyStable: false,
                            zoomCoverageChart: false
                }
                success {
                    colourText("info", "Stage: ${env.STAGE_NAME} successful!")
                }
                failure {
                    colourText("warn", "Stage: ${env.STAGE_NAME} failed!")
                }
            }
        }

        stage('Deploy: Algorithmia') {
            agent { label 'deploy.cf' }
            when {
                branch "master"
                // evaluate the when condition before entering this stage's agent, if any
                beforeAgent true
            }
            steps {
                colourText("warn", "Currently deployed to Algorithmia via ${env.TRAVIS_CI_URL}")
                colourText("info", "Changes pushed to Algorithmia master branch ${env.ALGO_GIT_URL}")
                colourText("info", "If deployment was successful test endpoint is ${env.ALGO_ENDPOINT}")
            }
            post {
                success {
                    colourText("info", "Stage: ${env.STAGE_NAME} successful!")
                }
                failure {
                    colourText("warn", "Stage: ${env.STAGE_NAME} failed!")
                }
            }
        }

        stage('Test: Algorithmia') {
            agent { label 'test.cf' }
            when {
                branch "master"
                // evaluate the when condition before entering this stage's agent, if any
                beforeAgent true
            }
            steps {
                colourText("warn", "Currently tested in Algorthmia via ${env.TRAVIS_CI_URL}")
                colourText("info", "If deployment was successful test endpoint is ${env.ALGO_ENDPOINT}")
            }
            post {
                success {
                    colourText("info", "Stage: ${env.STAGE_NAME} successful!")
                }
                failure {
                    colourText("warn", "Stage: ${env.STAGE_NAME} failed!")
                }
            }
        }
    }

    post {
        success {
            colourText("success", "All stages complete. Build was successful.")
            slackSend(
                    color: "good",
                    message: "${env.JOB_NAME} success: ${env.RUN_DISPLAY_URL}"
            )
        }
        unstable {
            colourText("warn", "Something went wrong, build finished with result ${currentResult}. This may be caused by failed tests, code violation or in some cases unexpected interrupt.")
            slackSend(
                    color: "warning",
                    message: "${env.JOB_NAME} unstable: ${env.RUN_DISPLAY_URL}"
            )
        }
        failure {
            colourText("warn","Process failed at: ${env.NODE_STAGE}")
            slackSend(
                    color: "danger",
                    message: "${env.JOB_NAME} failed at ${env.STAGE_NAME}: ${env.RUN_DISPLAY_URL}"
            )
        }
    }
}