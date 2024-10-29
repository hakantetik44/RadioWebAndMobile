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
        PDF_REPORT = "Test_Report_${TIMESTAMP}.pdf"
    }

    stages {
        stage('Initialize') {
            steps {
                script {
                    echo "Initializing Test Environment"
                }
                cleanWs()
                checkout scm

                sh '''
                    echo "Checking JAVA_HOME and Maven"
                    if [ -z "$JAVA_HOME" ]; then
                        echo "JAVA_HOME is not set!"
                        exit 1
                    fi
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
                    try {
                        echo "Running Tests..."
                        withEnv(["JAVA_HOME=${JAVA_HOME}"]) {
                            sh """
                                ${M2_HOME}/bin/mvn test \
                                -Dtest=runner.TestRunner \
                                -Dcucumber.plugin="pretty,json:target/cucumber.json,io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm" \
                                -Dwebdriver.chrome.headless=true \
                                -Dwebdriver.chrome.args="--headless,--disable-gpu,--window-size=1920,1080" \
                                | tee execution.log
                            """
                        }
                    } catch (Exception e) {
                        currentBuild.result = 'FAILURE'
                        echo "An error occurred: ${e.message}"
                        throw e
                    }
                }
            }
        }

        stage('Generate Reports') {
            steps {
                script {
                    sh """
                        ${M2_HOME}/bin/mvn verify -DskipTests
                    """
                    allure([
                        includeProperties: false,
                        jdk: '',
                        properties: [],
                        reportBuildPolicy: 'ALWAYS',
                        results: [[path: ALLURE_RESULTS]]
                    ])

                    echo "Generating PDF Report..."
                    sh """
                        wkhtmltopdf ${BUILD_URL}cucumber-html-reports/overview-features.html ${PDF_REPORT}
                    """
                }
            }
            post {
                always {
                    archiveArtifacts artifacts: """
                        ${CUCUMBER_REPORTS}/**/*,
                        target/cucumber.json,
                        ${ALLURE_RESULTS}/**/*,
                        target/screenshots/**/*,
                        execution.log,
                        ${PDF_REPORT}
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
                def testResults = ""
                if (fileExists('execution.log')) {
                    testResults = readFile('execution.log').trim()
                }

                echo """
                    Test Execution Summary:
                    ${testResults}

                    Reports:
                    - Cucumber Report: ${BUILD_URL}cucumber-html-reports/overview-features.html
                    - Allure Report: ${BUILD_URL}allure/
                    - PDF Report: ${BUILD_URL}${PDF_REPORT}


            }
            cleanWs()
        }
    }
}
