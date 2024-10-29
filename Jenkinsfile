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

                sh '''
                    export JAVA_HOME=/usr/local/opt/openjdk@17
                    echo "JAVA_HOME = ${JAVA_HOME}"
                    echo "M2_HOME = ${M2_HOME}"
                    java -version
                    ${M2_HOME}/bin/mvn -version
                '''
            }
        }

        stage('Build & Dependencies') {
            steps {
                sh """
                    export JAVA_HOME=/usr/local/opt/openjdk@17
                    ${M2_HOME}/bin/mvn clean install -DskipTests
                """
            }
        }

        stage('Run Tests') {
            steps {
                script {
                    echo "🚀 Running Tests..."
                    sh """
                        export JAVA_HOME=/usr/local/opt/openjdk@17
                        ${M2_HOME}/bin/mvn test \
                        -Dtest=runner.TestRunner \
                        -Dcucumber.plugin="pretty,json:target/cucumber.json,io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm" \
                        -Dwebdriver.chrome.headless=true \
                        -Dwebdriver.chrome.args="--headless,--disable-gpu,--window-size=1920,1080" \
                        | tee execution.log
                    """
                }
            }
        }

        stage('Generate Reports') {
            steps {
                script {
                    sh """
                        export JAVA_HOME=/usr/local/opt/openjdk@17
                        ${M2_HOME}/bin/mvn verify -DskipTests
                    """

                    // Allure raporu oluştur
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
                    ╔══════════════════════════════════╗
                    ║       Test Execution Summary     ║
                    ╚══════════════════════════════════╝

                    📊 Test Results:
                    ${testResults}

                    📝 Reports:
                    - Cucumber Report: ${BUILD_URL}cucumber-html-reports/overview-features.html
                    - Allure Report: ${BUILD_URL}allure/

                    ✅ SUCCESS
                """
            }
        }
        failure {
            echo """
                ╔══════════════════════════════════╗
                ║       Test Execution Failed      ║
                ╚══════════════════════════════════╝

                ❌ FAILED: Check the logs for details
            """
        }
        always {
            cleanWs()
        }
    }
}