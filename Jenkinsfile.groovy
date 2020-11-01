node {
    label 'android'

    timestamps {
        ansiColor('xterm') {
            stage('Checkout') {
                checkout scm
            }
            stage('Setup') {
                properties([buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '1', daysToKeepStr: '', numToKeepStr: '5')), [$class: 'ScannerJobProperty', doNotScan: false], disableConcurrentBuilds()])
                step([$class: 'GitHubCommitStatusSetter', errorHandlers: [[$class: 'ChangingBuildStatusErrorHandler', result: 'UNSTABLE']], reposSource: [$class: 'ManuallyEnteredRepositorySource', url: 'https://github.com/marunjar/anewjkuapp'], statusResultSource: [$class: 'ConditionalStatusResultSource', results: [[$class: 'AnyBuildResult', message: 'Build started', state: 'PENDING']]]])
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
                echo ${currentBuild.currentResult}
                archiveArtifacts artifacts: '**/*.apk', caseSensitive: false, followSymlinks: false
                step([$class: 'GitHubCommitStatusSetter', errorHandlers: [[$class: 'ChangingBuildStatusErrorHandler', result: 'UNSTABLE']], reposSource: [$class: 'ManuallyEnteredRepositorySource', url: 'https://github.com/marunjar/anewjkuapp'], statusResultSource: [$class: 'ConditionalStatusResultSource', results: [[$class: 'AnyBuildResult', message: 'Build complete', state: 'SUCCESS']]]])
            }
            stage('Cleanup') {
                withGradle {
                    sh './gradlew clean'
                    sh './gradlew cleanBuildCache'
                }
            }
        }
    }
}