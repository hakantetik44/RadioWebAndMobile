pipeline {
    agent any

    tools {
        maven 'maven'         // Jenkins üzerinde tanımlı Maven
        jdk 'JDK17'           // Jenkins üzerinde tanımlı JDK17
        allure 'Allure'       // Jenkins üzerinde tanımlı Allure
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
    }

    stages {
        stage('Initialize') {
            steps {
                script {
                    echo """
                        ╔══════════════════════════════════╗
                        ║      Test Automation Start       ║
                        ╚══════════════════════════════════╝
                    """
                }
                cleanWs()
                checkout scm
                sh '''
                    echo "JAVA_HOME = ${JAVA_HOME}"
                    echo "M2_HOME = ${M2_HOME}"
                    echo "PATH = ${PATH}"

                    java -version
                    mvn -version || { echo "Maven is not available!"; exit 1; }
                '''
            }
        }

        stage('Build & Dependencies') {
            steps {
                sh "${M2_HOME}/bin/mvn clean install -DskipTests"
            }
        }

        stage('Run Tests') {
            steps {
                script {
                    echo "🚀 Running Tests..."
                    withEnv(["JAVA_HOME=${JAVA_HOME}"]) {
                        def testStatus = sh(
                            script: "${M2_HOME}/bin/mvn test -Dtest=runner.TestRunner -Dcucumber.plugin='pretty,json:target/cucumber.json,io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm'",
                            returnStatus: true
                        )
                        if (testStatus != 0) {
                            error("Tests failed. Check execution.log for details.")
                        }
                    }
                }
            }
        }

        stage('Generate Reports') {
            steps {
                script {
                    sh "${M2_HOME}/bin/mvn verify -DskipTests"
                    sh "mkdir -p ${CUCUMBER_REPORTS}" // Düzeltilmiş satır

                    allure([
                        includeProperties: false,
                        jdk: '',
                        properties: [],
                        reportBuildPolicy: 'ALWAYS',
                        results: [[path: ALLURE_RESULTS]]
                    ])
                }
            }
            post {
                always {
                    archiveArtifacts artifacts: "${CUCUMBER_REPORTS}/**/*, target/cucumber.json, ${ALLURE_RESULTS}/**/*, target/screenshots/**/*, execution.log", allowEmptyArchive: true
                    cucumber buildStatus: 'UNSTABLE', fileIncludePattern: '**/cucumber.json', jsonReportDirectory: 'target'
                }
            }
        }
    }

    post {
        success {
            echo "✅ All tests passed successfully!"
        }
        failure {
            echo "❌ Some tests failed. Check the logs and reports for details."
        }
        always {
            script {
                def testResults = ""
                if (fileExists('execution.log')) {
                    testResults = readFile('execution.log').trim()
                }

                echo """
                    ╔══════════════════════════════════╗
                    ║       Test Execution Summary     ║
                    ╚══════════════════════════════════╝

                    📊 Test Results:
                    ${testResults}

                    📝 Reports:
                    - Cucumber Report: ${BUILD_URL}cucumber-html-reports/overview-features.html
                    - Allure Report: ${BUILD_URL}allure/

                    ${currentBuild.result == 'SUCCESS' ? '✅ SUCCESS' : '❌ FAILED'}
                """
            }
            cleanWs()
        }
    }
}
