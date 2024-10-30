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
        EXCEL_REPORTS = 'target/rapports-tests'
    }

    stages {
        stage('Initialisation') {
            steps {
                script {
                    echo """
                        ╔══════════════════════════════════╗
                        ║   Démarrage de l'Automatisation  ║
                        ╚══════════════════════════════════╝
                    """

                    cleanWs()
                    checkout scm

                    // Vérification de la structure du projet
                    sh '''
                        echo "=== Vérification de la Structure du Projet ==="

                        # Création des répertoires
                        mkdir -p src/test/java/utils
                        mkdir -p ${CUCUMBER_REPORTS}
                        mkdir -p ${ALLURE_RESULTS}
                        mkdir -p ${EXCEL_REPORTS}
                        mkdir -p target/screenshots

                        # Vérification des fichiers de test
                        if [ ! -f "src/test/java/utils/TestInfo.java" ] || [ ! -f "src/test/java/utils/TestReportManager.java" ]; then
                            echo "ERREUR: Les fichiers de test sont manquants!"
                            exit 1
                        fi

                        echo "=== Vérification de l'Environnement ==="
                        echo "JAVA_HOME = ${JAVA_HOME}"
                        echo "M2_HOME = ${M2_HOME}"
                        echo "PATH = ${PATH}"

                        # Vérification de Java
                        if [ -z "$JAVA_HOME" ]; then
                            echo "ERREUR: JAVA_HOME n'est pas défini!"
                            exit 1
                        fi

                        echo "Version Java:"
                        "${JAVA_HOME}/bin/java" -version

                        echo "Version Maven:"
                        "${M2_HOME}/bin/mvn" -version
                    '''
                }
            }
        }

        stage('Construction') {
            steps {
                script {
                    try {
                        echo "📦 Installation des dépendances..."
                        sh """
                            ${M2_HOME}/bin/mvn clean install -DskipTests -B || {
                                echo "Échec de la construction Maven!"
                                exit 1
                            }
                        """
                    } catch (Exception e) {
                        echo "ERREUR lors de la construction: ${e.getMessage()}"
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
                        sh """
                            ${M2_HOME}/bin/mvn test \
                            -Dtest=runner.TestRunner \
                            -DplatformName=Web \
                            -Dbrowser=chrome \
                            -Dcucumber.plugin="pretty,json:target/cucumber.json,html:${CUCUMBER_REPORTS},io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm" \
                            -Dcucumber.features="src/test/resources/features" \
                            -B | tee execution.log
                        """
                    } catch (Exception e) {
                        echo "ERREUR lors de l'exécution des tests: ${e.getMessage()}"
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
                        echo "📊 Génération des rapports..."

                        sh """
                            ${M2_HOME}/bin/mvn verify -DskipTests
                        """

                        allure([
                            includeProperties: false,
                            jdk: '',
                            properties: [],
                            reportBuildPolicy: 'ALWAYS',
                            results: [[path: "${ALLURE_RESULTS}"]]
                        ])

                    } catch (Exception e) {
                        echo "ERREUR lors de la génération des rapports: ${e.getMessage()}"
                        currentBuild.result = 'UNSTABLE'
                        throw e
                    }
                }
            }
            post {
                always {
                    archiveArtifacts artifacts: """
                        ${CUCUMBER_REPORTS}/**/*,
                        target/cucumber.json,
                        ${ALLURE_RESULTS}/**/*,
                        target/screenshots/**/*,
                        ${EXCEL_REPORTS}/**/*,
                        *.xlsx,
                        execution.log
                    """, allowEmptyArchive: true

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
                    - Rapport Cucumber: ${BUILD_URL}cucumber-html-reports/overview-features.html
                    - Rapport Allure: ${BUILD_URL}allure/
                    - Rapports Excel: ${EXCEL_REPORTS}

                    Résultat: ${currentBuild.result ?: 'INCONNU'}
                    ${currentBuild.result == 'SUCCESS' ? '✅ SUCCÈS' : '❌ ÉCHEC'}
                """
            }
            cleanWs notFailBuild: true
        }

        failure {
            echo """
                ❌ Échec de la construction!
                Veuillez consulter les logs pour plus de détails.
                Dernière erreur: ${currentBuild.description ?: 'Aucune description d\'erreur disponible'}
            """
        }
    }
}