node {
    label 'android'

    timestamps {
        ansiColor('xterm') {
            stage('Checkout') {
                checkout scm
            }
            stage('Setup') {
                properties([buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '1', daysToKeepStr: '', numToKeepStr: '5')), [$class: 'ScannerJobProperty', doNotScan: false], disableConcurrentBuilds()])
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
                step([$class: 'GitHubCommitStatusSetter', statusBackrefSource: [$class: 'ManuallyEnteredBackrefSource', backref: '']])
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