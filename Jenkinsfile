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
        stage('Initialize') {
            steps {
                script {
                    echo """
                        ╔══════════════════════════════════╗
                        ║      Test Automation Start       ║
                        ╚══════════════════════════════════╝
                    """

                    cleanWs()
                    checkout scm

                    sh '''
                        echo "========== Environment Check =========="
                        echo "JAVA_HOME = ${JAVA_HOME}"
                        echo "M2_HOME = ${M2_HOME}"
                        echo "PATH = ${PATH}"

                        # Java kontrolü
                        if [ -z "$JAVA_HOME" ]; then
                            echo "ERROR: JAVA_HOME is not set!"
                            exit 1
                        fi

                        if [ ! -x "${JAVA_HOME}/bin/java" ]; then
                            echo "ERROR: Java executable not found!"
                            exit 1
                        fi

                        echo "Java version:"
                        "${JAVA_HOME}/bin/java" -version

                        echo "Maven version:"
                        "${M2_HOME}/bin/mvn" -version

                        # Dizin yapısını oluştur
                        mkdir -p ${CUCUMBER_REPORTS}
                        mkdir -p ${ALLURE_RESULTS}
                        mkdir -p ${EXCEL_REPORTS}
                        mkdir -p target/screenshots
                        mkdir -p src/test/java/utils
                    '''
                }
            }
        }

        stage('Build & Dependencies') {
            steps {
                script {
                    try {
                        echo "📦 Installing dependencies..."

                        // Apache POI bağımlılıklarını kontrol et ve ekle
                        sh '''
                            if ! grep -q "org.apache.poi" pom.xml; then
                                echo "Adding Apache POI dependencies..."
                                sed -i '.bak' '/<dependencies>/a\\
                                    <dependency>\\
                                        <groupId>org.apache.poi</groupId>\\
                                        <artifactId>poi</artifactId>\\
                                        <version>5.2.3</version>\\
                                    </dependency>\\
                                    <dependency>\\
                                        <groupId>org.apache.poi</groupId>\\
                                        <artifactId>poi-ooxml</artifactId>\\
                                        <version>5.2.3</version>\\
                                    </dependency>\\
                                ' pom.xml
                            fi
                        '''

                        sh """
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
                                ${M2_HOME}/bin/mvn test \
                                -Dtest=runner.TestRunner \
                                -DplatformName=Web \
                                -Dbrowser=chrome \
                                -Dcucumber.plugin="pretty,json:target/cucumber.json,html:${CUCUMBER_REPORTS},io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm" \
                                -Dcucumber.features="src/test/resources/features" \
                                -B | tee execution.log
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

                        // Maven verify ve Allure raporu
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

                        // Excel raporlarını arşivle
                        sh """
                            if [ -d "${EXCEL_REPORTS}" ]; then
                                echo "Archiving Excel reports from ${EXCEL_REPORTS}"
                                cp ${EXCEL_REPORTS}/*.xlsx ${WORKSPACE}/
                            else
                                echo "No Excel reports found in ${EXCEL_REPORTS}"
                            fi
                        """

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
                def testResults = fileExists('execution.log') ? readFile('execution.log').trim() : "No test results available"

                echo """
                    ╔══════════════════════════════════╗
                    ║       Test Execution Summary     ║
                    ╚══════════════════════════════════╝

                    📊 Test Results:
                    ${testResults}

                    📝 Reports:
                    - Cucumber Report: ${BUILD_URL}cucumber-html-reports/overview-features.html
                    - Allure Report: ${BUILD_URL}allure/
                    - Excel Reports: ${EXCEL_REPORTS}

                    Build Result: ${currentBuild.result ?: 'UNKNOWN'}
                    ${currentBuild.result == 'SUCCESS' ? '✅ SUCCESS' : '❌ FAILED'}
                """
            }
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