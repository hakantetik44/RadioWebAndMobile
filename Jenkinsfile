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
            }
        }

        stage('Build & Dependencies') {
            steps {
                sh """
                    export JAVA_HOME=/usr/local/opt/openjdk@17
                    ${M2_HOME}/bin/mvn clean install -DskipTests \
                    -B -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=error \
                    -Dorg.slf4j.simpleLogger.showDateTime=false \
                    -Dorg.slf4j.simpleLogger.showThreadName=false \
                    --no-transfer-progress
                """
            }
        }

        stage('Run Tests') {
            steps {
                script {
                    echo "🚀 Running Tests..."
                    sh '''
                        export JAVA_HOME=/usr/local/opt/openjdk@17

                        # Çıktıyı geçici bir dosyaya yönlendir
                        ${M2_HOME}/bin/mvn test \
                        -Dtest=runner.TestRunner \
                        -Dcucumber.plugin="pretty,json:target/cucumber.json,io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm" \
                        -Dwebdriver.chrome.headless=true \
                        -Dwebdriver.chrome.args="--headless,--disable-gpu,--window-size=1920,1080" \
                        -B -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=error \
                        -Dorg.slf4j.simpleLogger.showDateTime=false \
                        -Dorg.slf4j.simpleLogger.showThreadName=false \
                        --no-transfer-progress > temp_output.txt 2>&1

                        # Sadece önemli satırları filtrele ve formatla
                        awk '
                            # Cucumber adımlarını kontrol et ve formatla
                            /^[[:space:]]*(Given|When|Then|And)/ {
                                if ($0 ~ /passed/) {
                                    print "💚 " $0
                                } else if ($0 ~ /failed/) {
                                    print "❌ " $0
                                } else if ($0 ~ /skipped/) {
                                    print "⏭️ " $0
                                } else if ($0 ~ /pending/) {
                                    print "⏳ " $0
                                } else {
                                    print "   " $0
                                }
                                next
                            }

                            # URL bilgilerini kontrol et
                            /expectedUrl|actualUrl/ {
                                print "🔍 " $0
                                next
                            }

                            # Test sonuçlarını kontrol et
                            /Tests run:/ && !/Running/ {
                                print $0
                                next
                            }

                            # Build sonucunu kontrol et
                            /BUILD SUCCESS|BUILD FAILURE/ {
                                print $0
                                next
                            }
                        ' temp_output.txt > execution.log

                        # Geçici dosyayı sil
                        rm temp_output.txt
                    '''
                }
            }
        }

        stage('Generate Reports') {
            steps {
                script {
                    sh """
                        export JAVA_HOME=/usr/local/opt/openjdk@17
                        ${M2_HOME}/bin/mvn verify -DskipTests -B --no-transfer-progress
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
                def testResults = fileExists('execution.log') ? readFile('execution.log').trim() : "No test results available"
                echo """╔════════════════════════════════╗
║     Test Execution Summary     ║
╚════════════════════════════════╝

${testResults}

📝 Reports:
• Cucumber: ${BUILD_URL}cucumber-html-reports/overview-features.html
• Allure: ${BUILD_URL}allure/
"""
            }
            cleanWs notFailBuild: true
        }

        failure {
            script {
                def testResults = fileExists('execution.log') ? readFile('execution.log').trim() : "No test results available"
                echo """╔════════════════════════════════╗
║     Test Execution Failed      ║
╚════════════════════════════════╝

${testResults}

❌ Build Failed
"""
            }
            cleanWs notFailBuild: true
        }
    }
}