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
        SOURCE_PROJECT = '/Users/hakan/IdeaProjects/RadioWebAndMobile'
        PLATFORM_NAME = "Web"  // Default value
        BROWSER = "chrome"     // Default value
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

                    sh '''
                        echo "=== Création des répertoires ==="
                        mkdir -p src/test/java/utils
                        mkdir -p ${CUCUMBER_REPORTS}
                        mkdir -p ${ALLURE_RESULTS}
                        mkdir -p ${EXCEL_REPORTS}
                        mkdir -p target/screenshots
                    '''

                    sh """
                        echo "=== Copie du fichier TestManager ==="
                        if [ -f '${SOURCE_PROJECT}/src/test/java/utils/TestManager.java' ]; then
                            cp '${SOURCE_PROJECT}/src/test/java/utils/TestManager.java' src/test/java/utils/
                            echo "Fichier TestManager.java copié."
                        else
                            echo "ERREUR: TestManager.java non trouvé."
                        fi

                        echo "Vérification des fichiers copiés:"
                        ls -l src/test/java/utils/
                    """

                    echo "=== Vérification de l'environnement ==="
                    sh '''
                        echo "JAVA_HOME = ${JAVA_HOME}"
                        echo "M2_HOME = ${M2_HOME}"

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
                        sh """
                            ${M2_HOME}/bin/mvn test \
                            -Dtest=runner.TestRunner \
                            -DplatformName=${PLATFORM_NAME} \
                            -Dbrowser=${BROWSER} \
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
                        sh "${M2_HOME}/bin/mvn verify -DskipTests"

                        allure([
                            includeProperties: false,
                            jdk: '',
                            properties: [],
                            reportBuildPolicy: 'ALWAYS',
                            results: [[path: "${ALLURE_RESULTS}"]]
                        ])

                        // Allure raporunu ziple
                        sh """
                            if [ -d "${ALLURE_RESULTS}" ]; then
                                cd target
                                zip -r allure-report.zip allure-results/
                                echo "Allure report ziplendi"
                            else
                                echo "Allure results dizini bulunamadı"
                            fi
                        """

                    } catch (Exception e) {
                        echo "ERREUR lors de la génération des rapports: ${e.getMessage()}"
                        currentBuild.result = 'UNSTABLE'
                    }
                }
            }
            post {
                always {
                    // Sadece Excel ve Allure raporlarını arşivle
                    archiveArtifacts artifacts: """
                        ${EXCEL_REPORTS}/**/*.xlsx,
                        target/allure-report.zip
                    """, allowEmptyArchive: true

                    // Cucumber raporu oluştur ama arşivleme
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
                    - Rapports Excel: ${BUILD_URL}artifact/${EXCEL_REPORTS}/

                    Résultat: ${currentBuild.result ?: 'INCONNU'}
                    Plateforme: ${PLATFORM_NAME}
                    Navigateur: ${BROWSER}
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