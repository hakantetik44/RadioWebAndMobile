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

                    // Workspace temizleme
                    cleanWs()

                    // SCM checkout
                    checkout scm

                    // Ortam kontrolü
                    sh '''
                        echo "========== Environment Check =========="
                        echo "JAVA_HOME = ${JAVA_HOME}"
                        echo "M2_HOME = ${M2_HOME}"
                        echo "PATH = ${PATH}"
                        echo "======================================="

                        # Java kontrolü
                        if [ -z "$JAVA_HOME" ]; then
                            echo "ERROR: JAVA_HOME is not set!"
                            exit 1
                        fi

                        if [ ! -x "${JAVA_HOME}/bin/java" ]; then
                            echo "ERROR: Java executable not found at ${JAVA_HOME}/bin/java"
                            exit 1
                        fi

                        echo "Java version:"
                        "${JAVA_HOME}/bin/java" -version

                        # Maven kontrolü
                        echo "Maven version:"
                        "${M2_HOME}/bin/mvn" -version || {
                            echo "ERROR: Maven check failed!"
                            exit 1
                        }

                        # Gerekli dizinlerin oluşturulması
                        mkdir -p ${CUCUMBER_REPORTS}
                        mkdir -p ${ALLURE_RESULTS}
                        mkdir -p target/screenshots
                    '''
                }
            }
        }

        stage('Build & Dependencies') {
            steps {
                script {
                    try {
                        echo "📦 Installing dependencies..."
                        sh """
                            echo "Running Maven clean install..."
                            ${M2_HOME}/bin/mvn clean install -DskipTests -B || {
                                echo "Maven build failed!"
                                exit 1
                            }
                        """
                    } catch (Exception e) {
                        echo "ERROR in Build & Dependencies stage: ${e.getMessage()}"
                        throw e
                    }
                }
            }
        }

        stage('Run Tests') {
            steps {
                script {
                    try {
                        echo "🧪 Running Tests..."
                        withEnv(["JAVA_HOME=${JAVA_HOME}"]) {
                            sh """
                                echo "Starting test execution..."
                                ${M2_HOME}/bin/mvn test \
                                -Dtest=runner.TestRunner \
                                -Dcucumber.plugin="pretty,json:target/cucumber.json,html:${CUCUMBER_REPORTS},io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm" \
                                -Dcucumber.features="src/test/resources/features" \
                                -B | tee execution.log

                                if [ \$? -ne 0 ]; then
                                    echo "Test execution failed!"
                                    exit 1
                                fi
                            """
                        }
                    } catch (Exception e) {
                        echo "ERROR in Test Execution: ${e.getMessage()}"
                        currentBuild.result = 'FAILURE'
                        throw e
                    }
                }
            }
        }

        stage('Generate Reports') {
            steps {
                script {
                    try {
                        echo "📊 Generating Reports..."

                        // Maven verify for reports
                        sh """
                            echo "Running Maven verify..."
                            ${M2_HOME}/bin/mvn verify -DskipTests
                        """

                        // Allure report generation
                        allure([
                            includeProperties: false,
                            jdk: '',
                            properties: [],
                            reportBuildPolicy: 'ALWAYS',
                            results: [[path: "${ALLURE_RESULTS}"]]
                        ])

                        echo "Reports generated successfully"
                    } catch (Exception e) {
                        echo "ERROR in Report Generation: ${e.getMessage()}"
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
                        execution.log
                    """, allowEmptyArchive: true

                    cucumber buildStatus: 'UNSTABLE',
                            fileIncludePattern: '**/cucumber.json',
                            jsonReportDirectory: 'target',
                            sortingMethod: 'ALPHABETICAL'
                }
            }
        }
    }

    post {
        always {
            script {
                def testResults = ""
                if (fileExists('execution.log')) {
                    testResults = readFile('execution.log').trim()
                    echo "Test execution log content: ${testResults}"
                } else {
                    echo "Warning: execution.log file not found"
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

                    Build Result: ${currentBuild.result ?: 'UNKNOWN'}
                    ${currentBuild.result == 'SUCCESS' ? '✅ SUCCESS' : '❌ FAILED'}
                """
            }

            // Workspace cleanup
            cleanWs notFailBuild: true
        }

        failure {
            script {
                echo """
                    ❌ Build failed!
                    Please check the logs for more details.
                    Last error: ${currentBuild.description ?: 'No error description available'}
                """
            }
        }
    }
}