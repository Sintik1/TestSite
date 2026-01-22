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
                echo 'Running Maven tests...'
                script {
                    // Используем Maven через Docker, так как в Jenkins контейнере нет Maven
                    sh '''
                        docker run --rm \
                            -v ${WORKSPACE}:/workspace \
                            -w /workspace \
                            -e SELENIUM_REMOTE_URL=${SELENIUM_REMOTE_URL} \
                            -e BASE_URI=${BASE_URI} \
                            -e CI=${CI} \
                            --network goals-network \
                            maven:3.9-eclipse-temurin-21 \
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
