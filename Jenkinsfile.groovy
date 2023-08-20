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
                    sh './gradlew --version'
                    sh './gradlew clean'
                }
            }
        }
        stage('Build') {
            steps {
                withGradle {
                    sh './gradlew assembleGoogle --stacktrace'
                    sh './gradlew bundleGoogle --stacktrace'
                    sh './gradlew assembleFdroid --stacktrace'
                    sh './gradlew bundleFdroid --stacktrace'
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
            recordIssues skipBlames: true, skipPublishingChecks: true, sourceDirectory: 'app/src', tools: [androidLintParser(pattern: '**/reports/lint-results-*.xml')]
            influxDbPublisher customPrefix: 'anewjkuapp', customProjectName: '', jenkinsEnvParameterField: '', jenkinsEnvParameterTag: '', selectedTarget: 'jenkins'
        }
        always {
            step([$class: 'GitHubCommitStatusSetter'])
            findBuildScans()
        }
        cleanup {
            mail to: 'jenkins.paul@inzinghof.at',
            subject: "${JOB_NAME} » ${BRANCH_NAME} - Build ${BUILD_DISPLAY_NAME}",
            body: '''${JOB_URL}'''

            emailext to: 'jenkins.paul@inzinghof.at',
            body: '''$PROJECT_NAME - Build # $BUILD_NUMBER - $BUILD_STATUS:

Check console output at $BUILD_URL to view the results.''', 
            recipientProviders: [buildUser(), requestor()], 
            subject: '$PROJECT_NAME - Build # $BUILD_NUMBER - $BUILD_STATUS!',
            attachLog: true
        }
    }
}
