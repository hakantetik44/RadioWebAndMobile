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

                    cleanWs()
                    checkout scm

                    // Verify and create directories
                    sh '''
                        mkdir -p src/test/java/utils
                        mkdir -p ${CUCUMBER_REPORTS}
                        mkdir -p ${ALLURE_RESULTS}
                        mkdir -p target/screenshots
                        mkdir -p target/rapports-tests
                    '''

                    // Create InfosTest.java
                    writeFile file: 'src/test/java/utils/InfosTest.java', text: '''
                        package utils;

                        import java.time.LocalDateTime;

                        public class InfosTest {
                            private String nomScenario;
                            private String nomEtape;
                            private String statut;
                            private String plateforme;
                            private String resultatAttendu;
                            private String resultatReel;
                            private String messageErreur;
                            private String url;
                            private LocalDateTime heureExecution;

                            public InfosTest() {
                                this.heureExecution = LocalDateTime.now();
                            }

                            // Getters ve Setters metodları...
                            // (Önceki kodda verilen tüm getter ve setter metodları)
                        }
                    '''

                    // Create GestionnaireRapportTest.java
                    writeFile file: 'src/test/java/utils/GestionnaireRapportTest.java', text: '''
                        package utils;

                        import org.apache.poi.ss.usermodel.*;
                        import org.apache.poi.xssf.usermodel.XSSFWorkbook;
                        import java.io.FileOutputStream;
                        import java.io.IOException;
                        import java.nio.file.Files;
                        import java.nio.file.Path;
                        import java.nio.file.Paths;
                        import java.time.format.DateTimeFormatter;
                        import java.util.ArrayList;
                        import java.util.List;

                        // (Önceki kodda verilen tüm GestionnaireRapportTest sınıfı)
                    '''

                    // Check Java and Maven
                    sh '''
                        echo "========== Environment Check =========="
                        echo "JAVA_HOME = ${JAVA_HOME}"
                        echo "M2_HOME = ${M2_HOME}"
                        echo "PATH = ${PATH}"

                        if [ -z "$JAVA_HOME" ]; then
                            echo "ERROR: JAVA_HOME is not set!"
                            exit 1
                        fi

                        java -version
                        mvn -version
                    '''
                }
            }
        }

        stage('Build & Dependencies') {
            steps {
                script {
                    try {
                        echo "📦 Installing dependencies..."

                        // Update pom.xml with required dependencies
                        sh '''
                            if ! grep -q "org.apache.poi" pom.xml; then
                                echo "Adding Apache POI dependency..."
                                sed -i'' -e '/<dependencies>/a\\
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

                        // Build project
                        sh """
                            ${M2_HOME}/bin/mvn clean install -DskipTests
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
                                -Dcucumber.plugin="pretty,json:target/cucumber.json,html:${CUCUMBER_REPORTS},io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm" \
                                -Dcucumber.features="src/test/resources/features" \
                                | tee execution.log
                            """
                        }
                    } catch (Exception e) {
                        currentBuild.result = 'FAILURE'
                        echo "ERROR in Test Execution: ${e.getMessage()}"
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

                        // Generate Cucumber reports
                        sh """
                            ${M2_HOME}/bin/mvn verify -DskipTests
                        """

                        // Generate Allure report
                        allure([
                            includeProperties: false,
                            jdk: '',
                            properties: [],
                            reportBuildPolicy: 'ALWAYS',
                            results: [[path: "${ALLURE_RESULTS}"]]
                        ])

                        // Generate Excel report using GestionnaireRapportTest
                        sh """
                            echo "Generating Excel Report..."
                            java -cp target/test-classes utils.GestionnaireRapportTest "RadioFrance_${TIMESTAMP}"
                        """

                    } catch (Exception e) {
                        echo "ERROR in Report Generation: ${e.getMessage()}"
                        currentBuild.result = 'UNSTABLE'
                    }
                }
            }
            post {
                always {
                    // Archive test artifacts
                    archiveArtifacts artifacts: """
                        ${CUCUMBER_REPORTS}/**/*,
                        target/cucumber.json,
                        ${ALLURE_RESULTS}/**/*,
                        target/screenshots/**/*,
                        target/rapports-tests/**/*,
                        execution.log
                    """, allowEmptyArchive: true

                    // Publish Cucumber report
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
                } else {
                    echo "Warning: execution.log not found"
                }

                echo """
                    ╔══════════════════════════════════╗
                    ║       Test Execution Summary     ║
                    ╚══════════════════════════════════╝

                    📊 Test Results:
                    ${testResults}

                    📝 Reports Available:
                    - Cucumber Report: ${BUILD_URL}cucumber-html-reports/overview-features.html
                    - Allure Report: ${BUILD_URL}allure/
                    - Excel Report: ${WORKSPACE}/target/rapports-tests/

                    Build Result: ${currentBuild.result ?: 'UNKNOWN'}
                    ${currentBuild.result == 'SUCCESS' ? '✅ SUCCESS' : '❌ FAILED'}
                """
            }

            cleanWs notFailBuild: true
        }

        success {
            echo """
                ✅ Build Successful!
                Test execution completed successfully.
                All reports have been generated.
            """
        }

        failure {
            echo """
                ❌ Build Failed!
                Please check the logs for more details.
                Last error: ${currentBuild.description ?: 'No error description available'}
            """
        }

        unstable {
            echo """
                ⚠️ Build Unstable!
                Some tests may have failed but the build completed.
                Please check the test reports for details.
            """
        }
    }
}