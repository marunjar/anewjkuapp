node {
    label 'android'

    timestamps {
        ansiColor('xterm') {
            stage('Checkout') {
                echo 'Checkout.'
                checkout scm
            }
            stage('Build') {
                echo 'Building..'
                withGradle {
                    sh './gradlew assembleFdroid'
                }

                echo 'Building..'
                withGradle {
                    sh './gradlew assembleGoogle'
                }
            }
            stage('Test') {
                echo 'Testing...'
            }
            stage('Analyze') {
                echo 'Analyzing....'
                try {
                    withGradle {
                        sh './gradlew lintFdroidDebug'
                    }
                } finally {
                    recordIssues blameDisabled: true, forensicsDisabled: true, skipPublishingChecks: true, sourceDirectory: './app/src', tools: [androidLintParser(pattern: './app/build/reports/lint-results-fdroidDebug.xml'), errorProne()]
                }
            }
            stage('Deploy') {
                echo 'Deploying.....'
                archiveArtifacts artifacts: './release/**/*.apk', caseSensitive: false, followSymlinks: false
            }
        }
    }
}