node {
    label 'android'

    timestamps {
        ansiColor('xterm') {
            stage('Checkout') {
                checkout scm
            }
            stage('Setup') {
                properties([buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '1', daysToKeepStr: '', numToKeepStr: '5')), [$class: 'ScannerJobProperty', doNotScan: false], disableConcurrentBuilds()])
                step([$class: 'GitHubCommitStatusSetter', errorHandlers: [[$class: 'ChangingBuildStatusErrorHandler', result: 'UNSTABLE']], statusResultSource: [$class: 'ConditionalStatusResultSource', results: [[$class: 'AnyBuildResult', message: "Build #${env.BUILD_NUMBER} in progress...", state: 'PENDING']]]])
                withGradle {
                    sh './gradlew clean'
                    sh './gradlew cleanBuildCache'
                }
            }
            stage('Build') {
                withGradle {
                    sh './gradlew assembleFdroid'
                    sh './gradlew assembleGoogle'
                }
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
                archiveArtifacts artifacts: '**/*.apk', caseSensitive: false, followSymlinks: false
            }
            stage('Cleanup') {
                step([$class: 'GitHubCommitStatusSetter', errorHandlers: [[$class: 'ChangingBuildStatusErrorHandler', result: 'UNSTABLE']], statusResultSource: [$class: 'ConditionalStatusResultSource', results: [[$class: 'AnyBuildResult', message: "Build #${env.BUILD_NUMBER} finished!", state: 'SUCCESS']]]])
            }
        }
    }
}