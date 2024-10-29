pipeline {
    agent any

    tools {
        maven 'MAVEN3'
        jdk 'JDK17'
    }

    environment {
        JAVA_HOME = tool 'JDK17'
        PATH = "${JAVA_HOME}/bin:${PATH}"
        MAVEN_OPTS = '-Xmx3072m -XX:MaxPermSize=512m'
        PROJECT_NAME = 'Radio BDD Automations Tests'
        TIMESTAMP = new Date().format('yyyy-MM-dd_HH-mm-ss')
        CUCUMBER_REPORTS = 'target/cucumber-reports'
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

                // Environment Check
                sh '''
                    echo "JAVA_HOME = ${JAVA_HOME}"
                    java -version
                    mvn -version
                '''
            }
        }

        stage('Build & Dependencies') {
            steps {
                sh """
                    mvn clean install -DskipTests
                    mvn checkstyle:check
                """
            }
        }

        stage('Run Tests') {
            steps {
                script {
                    try {
                        echo "🚀 Running Tests..."
                        sh """
                            mvn test \
                            -Dtest=runner.TestRunner \
                            -Dcucumber.plugin="pretty,json:target/cucumber.json,utils.formatter.PrettyReports:target/cucumber-pretty-reports" \
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
                sh """
                    mvn verify -DskipTests
                    mkdir -p ${CUCUMBER_REPORTS}
                """
            }
            post {
                always {
                    archiveArtifacts artifacts: """
                        target/cucumber-pretty-reports/**/*,
                        target/cucumber.json,
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

                    ${currentBuild.result == 'SUCCESS' ? '✅ SUCCESS' : '❌ FAILED'}
                """
            }
            cleanWs()
        }
    }
}