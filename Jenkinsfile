pipeline {
    agent any
    
    environment {
        SELENIUM_REMOTE_URL = 'http://selenium:4444/wd/hub'
        BASE_URI = 'http://frontend-service:9090'
        CI = 'true'
    }
    
    triggers {
        // Автозапуск при push в Git (GitHub/GitLab)
        // Раскомментируйте нужный вариант:
        
        //Для GitHub:
        githubPush()
        
        // Для GitLab:
        // gitlab(triggerOnPush: true, triggerOnMergeRequest: true)
        
        // ИЛИ по расписанию (каждый день в 11:00 утра)
        cron('H 11 * * *')
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out code from Git...'
                checkout scm
            }
        }
        
        stage('Test') {
            steps {
                echo 'Setting up Maven and running tests...'
                script {
                    sh '''
                        # Устанавливаем Maven, если его нет
                        if ! command -v mvn &> /dev/null; then
                            echo "Downloading Maven..."
                            MAVEN_VERSION=3.9.9
                            MAVEN_DIR=${WORKSPACE}/.maven
                            MAVEN_HOME=${MAVEN_DIR}/apache-maven-${MAVEN_VERSION}
                            
                            if [ ! -d "${MAVEN_HOME}" ]; then
                                mkdir -p ${MAVEN_DIR}
                                cd ${MAVEN_DIR}
                                wget -q https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz
                                tar -xzf apache-maven-${MAVEN_VERSION}-bin.tar.gz
                                rm apache-maven-${MAVEN_VERSION}-bin.tar.gz
                            fi
                            
                            export PATH=${MAVEN_HOME}/bin:${PATH}
                            echo "Maven installed: $(mvn -version)"
                        else
                            echo "Maven already available: $(mvn -version)"
                        fi
                        
                        # Запускаем тесты
                        mvn clean test
                    '''
                }
            }
            post {
                always {
                    // Сохранить отчёты тестов для Jenkins
                    junit 'target/surefire-reports/*.xml'
                }
                success {
                    echo '✅ All tests passed!'
                }
                failure {
                    echo '❌ Tests failed! Check console output for details.'
                }
            }
        }
    }
    
    post {
        always {
            echo 'Pipeline finished.'
        }
        failure {
            echo 'Pipeline failed. Check logs above.'
        }
    }
}
