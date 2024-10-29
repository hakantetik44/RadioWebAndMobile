pipeline {
    agent any

    tools {
        maven 'maven' // Jenkins üzerinde tanımlı Maven
        jdk 'JDK17' // Jenkins üzerinde tanımlı olan JDK17
        allure 'Allure' // Jenkins üzerinde tanımlı Allure
    }

    environment {
        JAVA_HOME = "/usr/local/opt/openjdk@17" // Güncellenmiş JAVA_HOME
        M2_HOME = tool 'maven' // Maven'ı Jenkins'ten al
        PATH = "${JAVA_HOME}/bin:${M2_HOME}/bin:/usr/local/bin:${PATH}" // Doğru PATH ayarı
        MAVEN_OPTS = '-Xmx3072m'
        PROJECT_NAME = 'Radio BDD Automation Tests'
        TIMESTAMP = new Date().format('yyyy-MM-dd_HH-mm-ss')
        CUCUMBER_REPORTS = 'target/cucumber-reports'
        ALLURE_RESULTS = 'target/allure-results'
        PDF_REPORT = "Test_Report_${TIMESTAMP}.pdf" // PDF rapor dosyası
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
                    echo "\033[0;36mJAVA_HOME = ${JAVA_HOME}\033[0m"
                    echo "\033[0;36mM2_HOME = ${M2_HOME}\033[0m"
                    echo "\033[0;36mPATH = ${PATH}\033[0m"

                    if [ -z "$JAVA_HOME" ]; then
                        echo "\033[0;31mJAVA_HOME is not set!\033[0m"
                        exit 1
                    fi

                    if [ ! -x "${JAVA_HOME}/bin/java" ]; then
                        echo "\033[0;31mJava is not available in JAVA_HOME!\033[0m"
                        exit 1
                    fi

                    java -version
                    mvn -version || { echo "\033[0;31mMaven is not available!\033[0m"; exit 1; }
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
                        echo "\033[0;32m🚀 Running Tests in Headless Mode...\033[0m"
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
                        mkdir -p ${CUCUMBER_REPORTS}
                    """

                    allure([
                        includeProperties: false,
                        jdk: '',
                        properties: [],
                        reportBuildPolicy: 'ALWAYS',
                        results: [[path: ALLURE_RESULTS]]
                    ])

                    // HTML raporunu PDF'ye dönüştür
                    echo "\033[0;34mGenerating PDF Report...\033[0m"
                    sh """
                        if command -v wkhtmltopdf &> /dev/null; then
                            wkhtmltopdf ${BUILD_URL}cucumber-html-reports/overview-features.html ${PDF_REPORT}
                        else
                            echo "\033[0;31mError: wkhtmltopdf not found!\033[0m"
                            exit 1
                        fi
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
                    ╔══════════════════════════════════╗
                    ║       Test Execution Summary     ║
                    ╚══════════════════════════════════╝

                    📊 Test Results:
                    ${testResults}

                    📝 Reports:
                    - Cucumber Report: ${BUILD_URL}cucumber-html-reports/overview-features.html
                    - Allure Report: ${BUILD_URL}allure/
                    - PDF Report: ${BUILD_URL}${PDF_REPORT}

                    ${currentBuild.result == 'SUCCESS' ? '✅ SUCCESS' : '❌ FAILED'}
                """
            }
            cleanWs()
        }
    }
}
