node {
    label 'android'

    timestamps {
        ansiColor('xterm') {
            try {
                stage('Checkout') {
                    checkout scm
                }
                stage('Setup') {
                    properties([buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '1', daysToKeepStr: '', numToKeepStr: '5')), [$class: 'ScannerJobProperty', doNotScan: false], disableConcurrentBuilds()])
                    step([$class: 'GitHubCommitStatusSetter', errorHandlers: [[$class: 'ChangingBuildStatusErrorHandler', result: 'UNSTABLE']]])
                }
                stage('Build') {
                    withGradle {
                        sh './gradlew assembleFdroid'
                        sh './gradlew assembleGoogle'
                    }
                    currentBuild.result = 'SUCCESS'
                }
                stage('Test') {
                    echo 'Testing...'
                }
                stage('Analyze') {
                    try {
                        withGradle {
                            sh './gradlew lintFdroidRelease'
                        }
                    } finally {
                        recordIssues blameDisabled: true, forensicsDisabled: true, skipPublishingChecks: true, sourceDirectory: 'app/src', tools: [androidLintParser(pattern: 'app/build/reports/lint-results-*.xml'), errorProne()]
                    }
                }
                stage('Deploy') {
                    echo '${currentBuild}'
                    archiveArtifacts artifacts: '**/*.apk', caseSensitive: false, followSymlinks: false
                }
            } catch (ignored) {
                currentBuild.result = 'FAILURE'
            }
            stage('Cleanup') {
                step([$class: 'GitHubCommitStatusSetter', errorHandlers: [[$class: 'ChangingBuildStatusErrorHandler', result: 'UNSTABLE']]])
                withGradle {
                    sh './gradlew clean'
                    sh './gradlew cleanBuildCache'
                }
            }
        }
    }
}