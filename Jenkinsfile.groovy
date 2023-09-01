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
    tools {
        jdk 'android-temurin-jdk-17.0.8.1'
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
                    sh './gradlew clean --no-build-cache'
                }
            }
        }
        stage('Build') {
            steps {
                withGradle {
                    sh './gradlew assembleGoogle --stacktrace --no-build-cache'
                    sh './gradlew bundleGoogle --stacktrace --no-build-cache'
                    sh './gradlew assembleFdroid --stacktrace --no-build-cache'
                    sh './gradlew bundleFdroid --stacktrace --no-build-cache'
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
                    sh './gradlew lintGoogleRelease --no-build-cache'
                    sh './gradlew lintFdroidRelease --no-build-cache'
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
            subject: "${currentBuild.fullProjectName} » ${BRANCH_NAME} - Build ${currentBuild.displayName} - ${currentBuild.currentResult}",
            body: "for details see ${currentBuild.absoluteUrl}"

            emailext to: 'jenkins.paul@inzinghof.at',
            body: '''$PROJECT_NAME - Build # $BUILD_NUMBER - $BUILD_STATUS:

Check console output at $BUILD_URL to view the results.''', 
            recipientProviders: [buildUser(), requestor()], 
            subject: '$PROJECT_NAME - Build # $BUILD_NUMBER - $BUILD_STATUS!',
            attachLog: true
        }
    }
}
