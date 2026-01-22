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
        
        // Для GitHub:
        // githubPush()
        
        // Для GitLab:
        // gitlab(triggerOnPush: true, triggerOnMergeRequest: true)
        
        // ИЛИ по расписанию (каждый день в 2:00 ночи)
        cron('H 2 * * *')
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
                sh 'mvn clean test'
            }
            post {
                always {
                    // Сохранить отчёты тестов для Jenkins
                    junit 'target/surefire-reports/*.xml'
                    
                    // Показать результаты
                    script {
                        def testResults = readFile 'target/surefire-reports/TEST-TestPageAuthorization.xml'
                        echo "Test results:\n${testResults}"
                    }
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
