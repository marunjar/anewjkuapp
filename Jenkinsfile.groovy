pipeline {
    agent {
        label 'android'
    }
    options {
        skipDefaultCheckout()
        timestamps()
        ansiColor('xterm')
        buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '2', daysToKeepStr: '', numToKeepStr: '5'))
        disableConcurrentBuilds()
    }
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Setup') {
            steps {
                sh 'java -XshowSettings:vm -version'
                step([$class: 'GitHubSetCommitStatusBuilder'])
                withGradle {
                    sh './gradlew clean'
                }
            }
        }
        stage('Build') {
            steps {
                withGradle {
                    sh './gradlew assembleGoogle'
                    sh './gradlew bundleGoogle'
                    sh './gradlew assembleFdroid'
                    sh './gradlew bundleFdroid'
                }
            }
        }
        stage('Test') {
            steps {
                echo 'Testing...'
            }
        }
        stage('Analyze') {
            steps {
                withGradle {
                    sh './gradlew lintGoogleRelease'
                    sh './gradlew lintFdroidRelease'
                }
            }
        }
        stage('Deploy') {
            when {
                expression {
                    currentBuild.result == null || currentBuild.result == 'SUCCESS'
                }
            }
            steps {
                archiveArtifacts artifacts: '**/outputs/**/*.apk, **/outputs/**/*.aab', caseSensitive: false, followSymlinks: false
            }
        }
    }
    post {
        success {
            recordIssues skipBlames: true, skipPublishingChecks: true, sourceDirectory: 'app/src', tools: [androidLintParser(pattern: '**/reports/lint-results-*.xml'), errorProne()]
            influxDbPublisher customPrefix: 'anewjkuapp', customProjectName: '', jenkinsEnvParameterField: '', jenkinsEnvParameterTag: '', selectedTarget: 'jenkins'
        }
        always {
            step([$class: 'GitHubCommitStatusSetter'])
            emailext attachLog: true, body: '''$PROJECT_NAME - Build # $BUILD_NUMBER - $BUILD_STATUS:

Check console output at $BUILD_URL to view the results.''', recipientProviders: [buildUser(), requestor()], subject: '$PROJECT_NAME - Build # $BUILD_NUMBER - $BUILD_STATUS!'
        }
        cleanup {
            echo 'Cleanup...'
        }
    }
}
