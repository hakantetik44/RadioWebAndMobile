pipeline {
    agent any

    tools {
        maven 'maven'
        jdk 'JDK17'  // JDK ayarını burada kontrol edin
    }

    environment {
        JAVA_HOME = tool('JDK17') // JDK17'nin doğru bir şekilde ayarlandığından emin olun
        M2_HOME = tool('maven')
        PATH = "${JAVA_HOME}/bin:${M2_HOME}/bin:${PATH}"
        MAVEN_OPTS = '-Xmx3072m'
    }

    stages {
        stage('Initialize') {
            steps {
                cleanWs()
                echo """
                    ╔══════════════════════════════════╗
                    ║      Test Automation Start       ║
                    ╚══════════════════════════════════╝
                """
                script {
                    // JAVA_HOME ve M2_HOME değişkenlerini kontrol et
                    sh '''
                        echo "JAVA_HOME = ${JAVA_HOME}"
                        echo "M2_HOME = ${M2_HOME}"
                        java -version
                        mvn -version
                    '''
                }
            }
        }

        stage('Build & Dependencies') {
            steps {
                sh """
                    mvn clean install -DskipTests
                """
            }
        }

        stage('Run Tests') {
            steps {
                sh """
                    mvn test \
                    -Dtest=runner.TestRunner \
                    -Dcucumber.plugin="pretty,json:target/cucumber.json,io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm" \
                    | tee execution.log
                """
            }
        }

        stage('Generate Reports') {
            steps {
                sh "mvn verify -DskipTests"
            }
        }
    }

    post {
        always {
            echo """
                ╔══════════════════════════════════╗
                ║       Test Execution Summary     ║
                ╚══════════════════════════════════╝
            """
            archiveArtifacts artifacts: 'target/**/*', allowEmptyArchive: true
        }
    }
}
