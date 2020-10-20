node {
    label 'android'

    timestamps {
        ansiColor('xterm') {
            stage('Build') {
                echo 'Building.'
                withGradle {
                    sh './gradlew assembleFdroid'
                }
                echo 'Building.'
                withGradle {
                    sh './gradlew assembleGoogle'
                }
            }
            stage('Test') {
                echo 'Testing..'
            }
            stage('Analyze') {
                echo 'Analyzing...'
                withGradle {
                    try {
                        sh './gradlew lintFdroidDebug'
                    } finally {
                        scanForIssues blameDisabled: true, forensicsDisabled: true, sourceDirectory: './app/src', tool: androidLintParser(pattern: './app/build/reports/lint-results-fdroidDebug.xml')
                    }
                }
                withGradle {
                    sh './gradlew assembleFdroid'
                }
            }
            stage('Deploy') {
                echo 'Deploying....'
            }
        }
    }
}