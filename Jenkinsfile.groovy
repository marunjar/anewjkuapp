node {
    label 'android'

    timestamps {
        ansiColor('xterm') {
            def workspace = pwd()
            stage('Build') {
                echo 'Building.'
                echo "${workspace}"

                withGradle {
                    sh 'cd ${workspace} && gradlew assembleFdroid'
                }
                echo 'Building.'
                withGradle {
                    sh 'cd ${workspace} && gradlew assembleGoogle'
                }
            }
            stage('Test') {
                echo 'Testing..'
            }
            stage('Analyze') {
                echo 'Analyzing...'
                withGradle {
                    try {
                        sh 'cd ${workspace} && gradlew lintFdroidDebug'
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