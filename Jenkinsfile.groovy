node {
    label 'android'

    timestamps {
        ansiColor('xterm') {
            stage('Build') {
                steps {
                    echo 'Building.'
                    withGradle {
                        sh './gradlew assembleFdroid'
                    }
                }
                steps {
                    echo 'Building.'
                    withGradle {
                        sh './gradlew assembleGoogle'
                    }
                }
            }
            stage('Test') {
                steps {
                    echo 'Testing..'
                }
            }
            stage('Analyze') {
                steps {
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
            }
            stage('Deploy') {
                steps {
                    echo 'Deploying....'
                }
            }
        }
    }
}