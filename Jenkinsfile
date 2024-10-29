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
                }
                cleanWs()
                checkout scm

                sh """
                    export JAVA_HOME=/usr/local/opt/openjdk@17
                    echo "🔧 Environment Setup:"
                    echo "JAVA_HOME = ${JAVA_HOME}"
                    echo "M2_HOME = ${M2_HOME}"
                    java -version
                    ${M2_HOME}/bin/mvn -version
                """
            }
        }

        stage('Build & Dependencies') {
            steps {
                script {
                    try {
                        sh """
                            export JAVA_HOME=/usr/local/opt/openjdk@17
                            echo "📦 Installing Dependencies..."
                            ${M2_HOME}/bin/mvn clean install -DskipTests
                        """
                        echo "✅ Build Successful"
                    } catch (Exception e) {
                        echo "❌ Build Failed: ${e.message}"
                        throw e
                    }
                }
            }
        }

        stage('Run Tests') {
            steps {
                script {
                    echo "🚀 Starting Test Execution..."
                    sh """
                        export JAVA_HOME=/usr/local/opt/openjdk@17

                        echo "🧪 Running Tests..."
                        ${M2_HOME}/bin/mvn test \
                        -Dtest=runner.TestRunner \
                        -Dcucumber.plugin="pretty,json:target/cucumber.json,io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm" \
                        -Dwebdriver.chrome.headless=true \
                        -Dwebdriver.chrome.args="--headless,--disable-gpu,--window-size=1920,1080" \
                        | tee test-output.txt

                        echo "📝 Processing Test Results..."
                        echo "Test Execution Results:" > execution.log
                        echo "======================" >> execution.log
                        echo "" >> execution.log

                        cat test-output.txt | while IFS= read -r line; do
                            # Given/When/Then/And steps
                            if [[ \$line == *"Given "* ]] || [[ \$line == *"When "* ]] || [[ \$line == *"Then "* ]] || [[ \$line == *"And "* ]]; then
                                if [[ \$line == *"failed"* ]] || [[ \$line == *"Failed"* ]] || [[ \$line == *"FAILED"* ]]; then
                                    echo "❌ \$line" >> execution.log
                                else
                                    echo "✅ \$line" >> execution.log
                                fi
                            # URL validations
                            elif [[ \$line == *"expectedUrl"* ]] || [[ \$line == *"actualUrl"* ]]; then
                                echo "🔍 \$line" >> execution.log
                            # Pop-up and cookies info
                            elif [[ \$line == *"pop-up"* ]] || [[ \$line == *"cookie"* ]]; then
                                echo "ℹ️ \$line" >> execution.log
                            # Test results
                            elif [[ \$line == *"passed"* ]]; then
                                echo "✅ \$line" >> execution.log
                            elif [[ \$line == *"failed"* ]]; then
                                echo "❌ \$line" >> execution.log
                            elif [[ \$line == *"skipped"* ]]; then
                                echo "⏭️ \$line" >> execution.log
                            # Other lines
                            else
                                echo "\$line" >> execution.log
                            fi
                        done

                        echo "\\n📊 Test Summary:" >> execution.log
                        echo "=================" >> execution.log
                        TOTAL=\$(grep -c "Given\\|When\\|Then\\|And" execution.log)
                        PASSED=\$(grep -c "✅" execution.log)
                        FAILED=\$(grep -c "❌" execution.log)
                        SKIPPED=\$(grep -c "⏭️" execution.log)
                        INFO=\$(grep -c "ℹ️" execution.log)

                        echo "📈 Statistics:" >> execution.log
                        echo "Total Steps: \$TOTAL" >> execution.log
                        echo "✅ Passed: \$PASSED" >> execution.log
                        echo "❌ Failed: \$FAILED" >> execution.log
                        echo "⏭️ Skipped: \$SKIPPED" >> execution.log
                        echo "ℹ️ Info Messages: \$INFO" >> execution.log
                    """
                }
            }
        }

        stage('Generate Reports') {
            steps {
                script {
                    echo "📊 Generating Reports..."
                    sh """
                        export JAVA_HOME=/usr/local/opt/openjdk@17
                        ${M2_HOME}/bin/mvn verify -DskipTests
                    """

                    allure([
                        includeProperties: false,
                        jdk: '',
                        properties: [],
                        reportBuildPolicy: 'ALWAYS',
                        results: [[path: 'target/allure-results']]
                    ])
                }
            }
            post {
                always {
                    archiveArtifacts artifacts: """
                        target/cucumber-reports/**/*,
                        target/cucumber.json,
                        target/allure-results/**/*,
                        target/screenshots/**/*,
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
        success {
            script {
                def testResults = ""
                if (fileExists('execution.log')) {
                    testResults = readFile('execution.log').trim()
                }

                echo """
                    ╔═════════════════════════════════════╗
                    ║        Test Execution Report        ║
                    ╚═════════════════════════════════════╝

                    ${testResults}

                    📝 Detailed Reports:
                    ==================
                    🥒 Cucumber Report: ${BUILD_URL}cucumber-html-reports/overview-features.html
                    📊 Allure Report: ${BUILD_URL}allure/

                    ✨ Test Execution Completed Successfully!

                    Legend:
                    ======
                    ✅ Passed Step
                    ❌ Failed Step
                    ⏭️ Skipped Step
                    ℹ️ Information
                    🔍 Validation
                """
            }
        }
        failure {
            script {
                def testResults = ""
                if (fileExists('execution.log')) {
                    testResults = readFile('execution.log').trim()
                }

                echo """
                    ╔═════════════════════════════════════╗
                    ║        Test Execution Failed        ║
                    ╚═════════════════════════════════════╝

                    ❌ Failed Steps:
                    ${testResults.findAll(/.*❌.*/)?.join('\n') ?: 'No specific step failures found'}

                    📝 Complete Results:
                    ${testResults}

                    🔍 Check the reports for more details:
                    - Cucumber Report: ${BUILD_URL}cucumber-html-reports/overview-features.html
                    - Allure Report: ${BUILD_URL}allure/
                """
            }
        }
        always {
            cleanWs()
        }
    }
}