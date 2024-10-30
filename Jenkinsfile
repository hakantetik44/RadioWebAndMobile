pipeline {
    agent any

    tools {
        maven 'maven'
        jdk 'JDK17'
        allure 'Allure'
    }

    environment {
        JAVA_HOME = "/usr/local/opt/openjdk@17"
        M2_HOME = tool 'maven'
        PATH = "${JAVA_HOME}/bin:${M2_HOME}/bin:${PATH}"
        MAVEN_OPTS = '-Xmx3072m'
        PROJECT_NAME = 'Radio BDD Automation Tests'
        TIMESTAMP = new Date().format('yyyy-MM-dd_HH-mm-ss')
        CUCUMBER_REPORTS = 'target/cucumber-reports'
        ALLURE_RESULTS = 'target/allure-results'
        EXCEL_REPORTS = 'test-output/excel-reports'
    }

    stages {
        // ... diğer stage'ler aynı kalacak ...

        stage('Rapports') {
            steps {
                script {
                    try {
                        echo "📊 Génération des rapports..."
                        sh "${M2_HOME}/bin/mvn verify -DskipTests"

                        // Allure raporu oluştur
                        allure([
                            includeProperties: false,
                            jdk: '',
                            properties: [],
                            reportBuildPolicy: 'ALWAYS',
                            results: [[path: "${ALLURE_RESULTS}"]]
                        ])

                        // Allure raporunu zip'le
                        sh """
                            cd target
                            zip -r allure-report.zip allure-results/
                        """
                    } catch (Exception e) {
                        echo "ERREUR lors de la génération des rapports: ${e.getMessage()}"
                        currentBuild.result = 'UNSTABLE'
                    }
                }
            }
            post {
                always {
                    // Sadece Excel raporları ve Allure zip'ini arşivle
                    archiveArtifacts artifacts: """
                        ${EXCEL_REPORTS}/*.xlsx,
                        target/allure-report.zip
                    """, allowEmptyArchive: true

                    // Cucumber raporu hala oluşturulsun ama arşivlenmesin
                    cucumber buildStatus: 'UNSTABLE',
                            fileIncludePattern: '**/cucumber.json',
                            jsonReportDirectory: 'target'
                }
            }
        }
    }

    post {
        always {
            script {
                def testResults = fileExists('execution.log') ? readFile('execution.log').trim() : "Aucun résultat disponible"

                echo """
                    ╔══════════════════════════════════╗
                    ║     Résumé de l'Exécution       ║
                    ╚══════════════════════════════════╝

                    📊 Résultats des Tests:
                    ${testResults}

                    📝 Rapports:
                    - Rapport Excel: ${BUILD_URL}artifact/${EXCEL_REPORTS}/
                    - Rapport Allure: ${BUILD_URL}allure/

                    ${currentBuild.result == 'SUCCESS' ? '✅ SUCCÈS' : '❌ ÉCHEC'}
                """
            }
            cleanWs notFailBuild: true
        }

        failure {
            echo """
                ❌ Échec de la construction!
                Veuillez consulter les logs pour plus de détails.
            """
        }
    }
}