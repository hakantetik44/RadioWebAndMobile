pipeline {
    agent any

    environment {
        // MacOS Homebrew paths
        JAVA_HOME = sh(script: '/usr/libexec/java_home -v 17', returnStdout: true).trim()
        M2_HOME = '/usr/local/Cellar/maven/3.9.9/libexec'
        PATH = "${JAVA_HOME}/bin:${M2_HOME}/bin:${env.PATH}"

        // Project variables
        PROJECT_NAME = 'Radio BDD Automation Tests'
        TIMESTAMP = new Date().format('yyyy-MM-dd_HH-mm-ss')
        ALLURE_RESULTS = 'target/allure-results'
        EXCEL_REPORTS = 'target/rapports-tests'
    }

    parameters {
        choice(
            name: 'PLATFORM_NAME',
            choices: ['Web', 'Android', 'iOS'],
            description: 'Sélectionnez la plateforme de test'
        )
        choice(
            name: 'BROWSER',
            choices: ['chrome', 'firefox', 'safari'],
            description: 'Sélectionnez le navigateur (pour Web uniquement)'
        )
    }

    stages {
        stage('Initialisation') {
            steps {
                script {
                    echo "╔═══════════════════════════════╗\n║ Démarrage de l'Automatisation ║\n╚═══════════════════════════════╝"
                    cleanWs()
                    checkout scm

                    // Environment verification
                    sh '''#!/bin/bash
                        echo "=== Environment Verification ==="

                        # Set up environment variables
                        export JAVA_HOME=$(/usr/libexec/java_home -v 17)
                        export M2_HOME=/usr/local/Cellar/maven/3.9.9/libexec
                        export PATH=$JAVA_HOME/bin:$M2_HOME/bin:$PATH

                        # Create directories
                        mkdir -p ${EXCEL_REPORTS} ${ALLURE_RESULTS} target/screenshots

                        # Print environment info
                        echo "JAVA_HOME: $JAVA_HOME"
                        echo "M2_HOME: $M2_HOME"
                        echo "PATH: $PATH"

                        # Verify Java and Maven
                        echo "Java version:"
                        java -version

                        echo "Maven version:"
                        mvn -v
                    '''

                    if (fileExists('src/test/resources/configuration.properties')) {
                        def configContent = readFile('src/test/resources/configuration.properties')
                        def props = configContent.split('\n').collectEntries { line ->
                            def parts = line.split('=')
                            if (parts.size() == 2) {
                                [(parts[0].trim()): parts[1].trim()]
                            } else {
                                [:]
                            }
                        }

                        env.PLATFORM_NAME = props.platformName ?: params.PLATFORM_NAME ?: 'Web'
                        env.BROWSER = env.PLATFORM_NAME == 'Web' ? (props.browser ?: params.BROWSER ?: 'chrome') : ''
                    }

                    echo """Configuration actuelle:
                    • Plateforme: ${env.PLATFORM_NAME}
                    • Navigateur: ${env.PLATFORM_NAME == 'Web' ? env.BROWSER : 'N/A'}"""
                }
            }
        }

        stage('Construction') {
            steps {
                script {
                    try {
                        echo "📦 Installation des dépendances..."
                        sh '''#!/bin/bash
                            export JAVA_HOME=$(/usr/libexec/java_home -v 17)
                            export M2_HOME=/usr/local/Cellar/maven/3.9.9/libexec
                            export PATH=$JAVA_HOME/bin:$M2_HOME/bin:$PATH

                            mvn clean install -DskipTests
                        '''
                    } catch (Exception e) {
                        currentBuild.result = 'FAILURE'
                        throw e
                    }
                }
            }
        }

        stage('Exécution des Tests') {
            steps {
                script {
                    try {
                        echo "🧪 Lancement des tests..."
                        sh """#!/bin/bash
                            export JAVA_HOME=\$(/usr/libexec/java_home -v 17)
                            export M2_HOME=/usr/local/Cellar/maven/3.9.9/libexec
                            export PATH=\$JAVA_HOME/bin:\$M2_HOME/bin:\$PATH

                            mvn test -Dtest=runner.TestRunner \\
                                -DplatformName=${env.PLATFORM_NAME} \\
                                ${env.PLATFORM_NAME == 'Web' ? "-Dbrowser=${env.BROWSER}" : ''} \\
                                -Dcucumber.plugin="pretty,json:target/cucumber.json,io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
                        """
                    } catch (Exception e) {
                        currentBuild.result = 'FAILURE'
                        throw e
                    }
                }
            }
        }

        stage('Rapports') {
            steps {
                script {
                    try {
                        allure([
                            includeProperties: true,
                            reportBuildPolicy: 'ALWAYS',
                            results: [[path: "${ALLURE_RESULTS}"]]
                        ])

                        sh """
                            if [ -d "${ALLURE_RESULTS}" ]; then
                                cd target && zip -q -r allure-report.zip allure-results/
                            fi
                        """
                    } catch (Exception e) {
                        currentBuild.result = 'UNSTABLE'
                    }
                }
            }
            post {
                always {
                    archiveArtifacts artifacts: "${EXCEL_REPORTS}/**/*.xlsx, target/allure-report.zip", allowEmptyArchive: true
                }
            }
        }
    }

    post {
        always {
            script {
                def testResults = fileExists('test_output.log') ? readFile('test_output.log').trim() : "Aucun résultat disponible"

                echo """╔═══════════════════════════╗
║   Résumé de l'Exécution   ║
╚═══════════════════════════╝

📝 Rapports:
• Allure: ${BUILD_URL}allure/
• Excel: ${BUILD_URL}artifact/${EXCEL_REPORTS}/

Plateforme: ${env.PLATFORM_NAME}
${env.PLATFORM_NAME == 'Web' ? "Navigateur: ${env.BROWSER}" : ''}
${currentBuild.result == 'SUCCESS' ? '✅ SUCCÈS' : '❌ ÉCHEC'}"""
            }
            cleanWs notFailBuild: true
        }
    }
}