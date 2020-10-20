node {
    label 'android'

    timestamps {
        ansiColor('xterm') {
            def workspace = pwd()
            dir(workspace) {
                stage('Build') {
                    echo 'Building.'
                    echo workspace
                    sh 'ls -l'

                    withGradle {
                        sh 'gradlew assembleFdroid'
                    }
                    echo 'Building.'
                    withGradle {
                        sh 'gradlew assembleGoogle'
                    }
                }
                stage('Test') {
                    echo 'Testing..'
                }
                stage('Analyze') {
                    echo 'Analyzing...'
                    withGradle {
                        try {
                            sh 'gradlew lintFdroidDebug'
                        } finally {
                            scanForIssues blameDisabled: true, forensicsDisabled: true, sourceDirectory: 'app/src', tool: androidLintParser(pattern: 'app/build/reports/lint-results-fdroidDebug.xml')
                        }
                    }
                }
                stage('Deploy') {
                    echo 'Deploying....'
                }
            }
        }
    }
}