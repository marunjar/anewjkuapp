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
        jdk 'android-temurin-jdk-21'
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
                    sh './gradlew clean --refresh-dependencies'
                }
            }
        }
        stage('Build') {
            steps {
                withGradle {
                    sh './gradlew assembleFdroid assembleGoogle --stacktrace'
                    sh './gradlew bundleFdroid bundleGoogle --stacktrace'
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
                    sh './gradlew lintFdroidRelease lintGoogleRelease --warning-mode all --stacktrace'
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
            recordIssues skipBlames: true, skipPublishingChecks: true, sourceDirectories: [[path: 'app/src']], tools: [androidLintParser(pattern: '**/reports/lint-results-*.xml')]
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
