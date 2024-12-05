pipeline {
    agent any

    tools {
        maven 'maven'
        jdk 'JDK17'
        allure 'Allure'
    }

    environment {
        // Java ve Maven için kesin yolları kullan
        JAVA_HOME = '/Users/hakantetik/Library/Java/JavaVirtualMachines/corretto-17.0.13/Contents/Home'
        M2_HOME = '/usr/local/Cellar/maven/3.9.9/libexec'
        PATH = "${JAVA_HOME}/bin:${M2_HOME}/bin:${env.PATH}"

        // Proje değişkenleri
        PROJECT_NAME = 'Radio BDD Automation Tests'
        TIMESTAMP = new Date().format('yyyy-MM-dd_HH-mm-ss')
        ALLURE_RESULTS = 'target/allure-results'
        EXCEL_REPORTS = 'target/rapports-tests'
    }

    parameters {
        choice(
            name: 'PLATFORM_NAME',
            choices: ['Web', 'Android', 'iOS'],
            description: 'Test platformunu seçin'
        )
        choice(
            name: 'BROWSER',
            choices: ['chrome', 'firefox', 'safari'],
            description: 'Web platformu için tarayıcı seçin'
        )
    }

    stages {
        stage('Ortam Doğrulama') {
            steps {
                script {
                    echo "Ortam Kontrolü Başlatılıyor..."

                    // Ortam değişkenlerini ve araçları doğrula
                    sh '''
                        echo "Java Bilgileri:"
                        java -version

                        echo "Maven Bilgileri:"
                        mvn -version

                        echo "Java HOME: $JAVA_HOME"
                        echo "Maven HOME: $M2_HOME"
                    '''

                    // Gerekli dizinleri oluştur
                    sh 'mkdir -p target/screenshots target/allure-results target/rapports-tests'
                }
            }
        }

        stage('Bağımlılıkları Yükleme') {
            steps {
                script {
                    try {
                        sh '''
                            mvn clean install -DskipTests
                        '''
                    } catch (Exception e) {
                        currentBuild.result = 'FAILURE'
                        error "Bağımlılıkları yüklerken hata oluştu: ${e.message}"
                    }
                }
            }
        }

        stage('Test Çalıştırma') {
            steps {
                script {
                    try {
                        sh """
                            mvn test -Dtest=runner.TestRunner \\
                                -DplatformName=${params.PLATFORM_NAME} \\
                                ${params.PLATFORM_NAME == 'Web' ? "-Dbrowser=${params.BROWSER}" : ''} \\
                                -Dcucumber.plugin="pretty,json:target/cucumber.json,io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
                        """
                    } catch (Exception e) {
                        currentBuild.result = 'FAILURE'
                        error "Test çalıştırılırken hata oluştu: ${e.message}"
                    }
                }
            }
        }

        stage('Raporlama') {
            steps {
                script {
                    try {
                        allure([
                            includeProperties: true,
                            reportBuildPolicy: 'ALWAYS',
                            results: [[path: "${ALLURE_RESULTS}"]]
                        ])

                        // Allure rapor ve diğer raporları sıkıştır
                        sh '''
                            cd target
                            zip -r allure-report.zip allure-results/
                        '''
                    } catch (Exception e) {
                        currentBuild.result = 'UNSTABLE'
                        echo "Rapor oluşturulurken hata: ${e.message}"
                    }
                }
            }
            post {
                always {
                    archiveArtifacts artifacts: 'target/rapports-tests/**/*.xlsx, target/allure-report.zip', allowEmptyArchive: true
                }
            }
        }
    }

    post {
        always {
            script {
                echo """
╔═══════════════════════════╗
║   Test Çalıştırma Özeti   ║
╚═══════════════════════════╝

📋 Raporlar:
• Allure: ${BUILD_URL}allure/
• Excel Raporları: ${BUILD_URL}artifact/target/rapports-tests/

Platform: ${params.PLATFORM_NAME}
${params.PLATFORM_NAME == 'Web' ? "Tarayıcı: ${params.BROWSER}" : ''}
Sonuç: ${currentBuild.result == 'SUCCESS' ? '✅ BAŞARILI' : '❌ BAŞARISIZ'}
"""
            }
            cleanWs notFailBuild: true
        }
    }
}