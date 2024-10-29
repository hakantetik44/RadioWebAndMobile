pipeline {
    agent any

    tools {
        maven 'Maven 3.9.5'
        jdk 'JDK 17'
    }

    environment {
        MAVEN_OPTS = '-Xmx3072m -XX:MaxPermSize=512m'
        PROJECT_NAME = 'Radio BDD Automations Tests'
        TIMESTAMP = new Date().format('yyyy-MM-dd_HH-mm-ss')
        CUCUMBER_REPORTS = 'target/cucumber-reports'
        GIT_COMMIT_MSG = sh(script: 'git log -1 --pretty=%B', returnStdout: true).trim()
        GIT_AUTHOR = sh(script: 'git log -1 --pretty=%an', returnStdout: true).trim()
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

        stage('Run Web Tests') {
            steps {
                script {
                    try {
                        echo "🌐 Running Web Tests..."
                        sh """
                            mvn test \
                            -Dtest=WebTestRunner \
                            -Dcucumber.filter.tags="@web" \
                            -Dcucumber.plugin="json:target/cucumber-web.json,html:target/cucumber-reports/web" \
                            | tee web-execution.log
                        """
                    } catch (Exception e) {
                        currentBuild.result = 'FAILURE'
                        throw e
                    }
                }
            }
        }

        stage('Run Mobile Tests') {
            steps {
                script {
                    try {
                        echo "📱 Running Mobile Tests..."
                        sh """
                            mvn test \
                            -Dtest=MobileTestRunner \
                            -Dcucumber.filter.tags="@mobile" \
                            -Dcucumber.plugin="json:target/cucumber-mobile.json,html:target/cucumber-reports/mobile" \
                            | tee mobile-execution.log
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
                        target/cucumber-reports/**/*,
                        target/cucumber*.json,
                        target/screenshots/**/*,
                        *execution.log
                    """, allowEmptyArchive: true

                    cucumber buildStatus: 'UNSTABLE',
                            fileIncludePattern: '**/cucumber*.json',
                            jsonReportDirectory: 'target'
                }
            }
        }
    }

    post {
        always {
            script {
                def testResults = ""
                if (fileExists('*execution.log')) {
                    testResults = readFile('*execution.log').trim()
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