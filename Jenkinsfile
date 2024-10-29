pipeline {
    agent any

    tools {
        maven 'maven'
        jdk 'JDK17'
    }

    environment {
        JAVA_HOME = '/usr/local/opt/openjdk@17'
        M2_HOME = tool 'maven'
        PATH = "${JAVA_HOME}/bin:${M2_HOME}/bin:${PATH}"
        MAVEN_OPTS = '-Xmx3072m'  // MaxPermSize kaldırıldı
        PROJECT_NAME = 'Radio BDD Automations Tests'
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
                    ${M2_HOME}/bin/mvn checkstyle:check
                """
            }
        }

        stage('Run Tests') {
            steps {
                script {
                    try {
                        echo "🚀 Running Tests..."
                        sh """
                            export JAVA_HOME=/usr/local/opt/openjdk@17
                            ${M2_HOME}/bin/mvn test \
                            -Dtest=runner.TestRunner \
                            -Dcucumber.plugin="pretty,json:target/cucumber.json,utils.formatter.PrettyReports:target/cucumber-pretty-reports,io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm" \
                            | tee execution.log
                        """
                    } catch (Exception e) {
                        currentBuild.result = 'FAILURE'
                        throw e
                    }
                }
            }
        }

        stage('Generate Reports') {
            steps {
                script {
                    // Cucumber Reports
                    sh """
                        export JAVA_HOME=/usr/local/opt/openjdk@17
                        ${M2_HOME}/bin/mvn verify -DskipTests
                        mkdir -p ${CUCUMBER_REPORTS}
                    """

                    // Allure Report
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
                        target/cucumber-pretty-reports/**/*,
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