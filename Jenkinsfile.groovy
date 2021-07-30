node {
    label 'android'

    timestamps {
        ansiColor('xterm') {
            stage('Checkout') {
                echo 'Checkout...'
                checkout scm
            }
            stage('Setup') {
                properties([buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '2', daysToKeepStr: '', numToKeepStr: '5')), [$class: 'ScannerJobProperty', doNotScan: false], disableConcurrentBuilds()])
                step([$class: 'GitHubCommitStatusSetter', errorHandlers: [[$class: 'ChangingBuildStatusErrorHandler', result: 'UNSTABLE']], statusResultSource: [$class: 'ConditionalStatusResultSource', results: [[$class: 'AnyBuildResult', message: "Build #${env.BUILD_NUMBER} in progress...", state: 'PENDING']]]])
                withGradle {
                    sh './gradlew clean'
                }
            }
            stage('Build') {
                withGradle {
                    sh './gradlew assembleGoogle'
                    sh './gradlew bundleGoogle'
                    sh './gradlew assembleFdroid'
                    sh './gradlew bundleFdroid'
                }
            }
            stage('Test') {
                echo 'Testing...'
            }
            stage('Analyze') {
                try {
                    withGradle {
                        sh './gradlew lintGoogleRelease'
                        sh './gradlew lintFdroidRelease'
                    }
                } finally {
                    recordIssues blameDisabled: true, forensicsDisabled: true, skipPublishingChecks: true, sourceDirectory: 'app/src', tools: [androidLintParser(pattern: '**/reports/lint-results-*.xml'), errorProne()]
                }
            }
            stage('Deploy') {
                archiveArtifacts artifacts: '**/outputs/**/*.apk, **/outputs/**/*.aab', caseSensitive: false, followSymlinks: false
                influxDbPublisher customPrefix: 'anewjkuapp', customProjectName: '', jenkinsEnvParameterField: '', jenkinsEnvParameterTag: '', selectedTarget: 'jenkins'
            }
            stage('Cleanup') {
            }
            stage('Notify') {
                step([$class: 'GitHubCommitStatusSetter', errorHandlers: [[$class: 'ChangingBuildStatusErrorHandler', result: 'UNSTABLE']], statusResultSource: [$class: 'ConditionalStatusResultSource', results: [[$class: 'AnyBuildResult', message: "Build #${env.BUILD_NUMBER} finished!", state: 'SUCCESS']]]])
                emailext attachLog: true, body: '''$PROJECT_NAME - Build # $BUILD_NUMBER - $BUILD_STATUS:

Check console output at $BUILD_URL to view the results.''', recipientProviders: [buildUser(), requestor()], subject: '$PROJECT_NAME - Build # $BUILD_NUMBER - $BUILD_STATUS!'
            }
        }
    }
}