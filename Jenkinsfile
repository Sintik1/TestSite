pipeline {
    agent any
    
    environment {
        SELENIUM_REMOTE_URL = 'http://selenium:4444/wd/hub'
        BASE_URI = 'http://frontend-service:9090'
        CI = 'true'
        
        // Telegram Bot настройки (можно задать через Jenkins Credentials или переменные окружения)
        // Для использования credentials: credentials('telegram-bot-token')
        // Или напрямую: 'YOUR_BOT_TOKEN'
        TELEGRAM_BOT_TOKEN = ''
        TELEGRAM_CHAT_ID = ''
        
        // Email настройки
        EMAIL_TO = ''
        EMAIL_FROM = 'jenkins@example.com'
    }
    
    triggers {
        // Автозапуск при push в Git (GitHub/GitLab)
        //Для GitHub:
        githubPush()
        pollSCM('H/5 * * * *')
        
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
                                
                                # Пробуем скачать через curl, если нет - устанавливаем curl
                                if ! command -v curl &> /dev/null; then
                                    echo "Installing curl..."
                                    apt-get update -qq && apt-get install -y -qq curl
                                fi
                                
                                echo "Downloading Maven ${MAVEN_VERSION}..."
                                curl -L -o apache-maven-${MAVEN_VERSION}-bin.tar.gz https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz
                                
                                echo "Extracting Maven..."
                                tar -xzf apache-maven-${MAVEN_VERSION}-bin.tar.gz
                                rm apache-maven-${MAVEN_VERSION}-bin.tar.gz
                            fi
                            
                            export PATH=${MAVEN_HOME}/bin:${PATH}
                            echo "Maven installed: $(mvn -version)"
                        else
                            echo "Maven already available: $(mvn -version)"
                        fi
                        
                        # Возвращаемся в корень проекта (где находится pom.xml)
                        cd ${WORKSPACE}
                        
                        # Запускаем тесты
                        echo "Running tests from: $(pwd)"
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
            
            // Отправка уведомлений
            script {
                def status = currentBuild.result ?: 'SUCCESS'
                def buildUrl = env.BUILD_URL ?: 'N/A'
                def jobName = env.JOB_NAME ?: 'Test Pipeline'
                def buildNumber = env.BUILD_NUMBER ?: 'N/A'
                def gitCommit = env.GIT_COMMIT ?: 'N/A'
                def gitBranch = env.GIT_BRANCH ?: 'N/A'
                
                // Читаем результаты тестов
                def testResults = readTestResults()
                
                // Формируем сообщение
                def emoji = status == 'SUCCESS' ? '✅' : '❌'
                def statusText = status == 'SUCCESS' ? 'Успешно' : 'Провалено'
                def message = """
${emoji} *${jobName}* - Build #${buildNumber}

*Статус:* ${statusText}

*Результаты тестов:*
• Всего: ${testResults.total}
• Успешно: ${testResults.passed}
• Провалено: ${testResults.failed}
• Ошибки: ${testResults.errors}
• Пропущено: ${testResults.skipped}

*Информация:*
• Ветка: ${gitBranch}
• Коммит: ${gitCommit.take(7)}
• Ссылка: ${buildUrl}

Время: ${new Date()}
""".trim()
                
                // Отправка в Telegram
                if (env.TELEGRAM_BOT_TOKEN && env.TELEGRAM_CHAT_ID && 
                    !env.TELEGRAM_BOT_TOKEN.isEmpty() && !env.TELEGRAM_CHAT_ID.isEmpty()) {
                    sendTelegramNotification(message)
                } else {
                    echo '⚠️ Telegram credentials not configured. Set TELEGRAM_BOT_TOKEN and TELEGRAM_CHAT_ID.'
                }
                
                // Отправка Email
                if (env.EMAIL_TO && !env.EMAIL_TO.isEmpty()) {
                    sendEmailNotification(message, status, subject: "[${status}] ${jobName} - Build #${buildNumber}")
                } else {
                    echo '⚠️ Email not configured. Set EMAIL_TO environment variable.'
                }
            }
        }
        success {
            echo '✅ Pipeline completed successfully!'
        }
        failure {
            echo '❌ Pipeline failed. Check logs above.'
        }
    }
}

/**
 * Читает результаты тестов из surefire-reports
 */
def readTestResults() {
    def results = [total: 0, failed: 0, skipped: 0, errors: 0, passed: 0]
    
    try {
        def reportFiles = sh(
            script: 'find target/surefire-reports -name "*.xml" -type f 2>/dev/null || echo ""',
            returnStdout: true
        ).trim()
        
        if (reportFiles && !reportFiles.isEmpty()) {
            def testsTotal = 0
            def testsFailed = 0
            def testsSkipped = 0
            def testsErrors = 0
            
            reportFiles.split('\n').each { file ->
                if (file && file.trim()) {
                    try {
                        def content = readFile(file: file.trim())
                        def totalMatch = (content =~ /tests="(\d+)"/)
                        def failedMatch = (content =~ /failures="(\d+)"/)
                        def skippedMatch = (content =~ /skipped="(\d+)"/)
                        def errorsMatch = (content =~ /errors="(\d+)"/)
                        
                        if (totalMatch) {
                            testsTotal += totalMatch[0][1].toInteger()
                        }
                        if (failedMatch) {
                            testsFailed += failedMatch[0][1].toInteger()
                        }
                        if (skippedMatch) {
                            testsSkipped += skippedMatch[0][1].toInteger()
                        }
                        if (errorsMatch) {
                            testsErrors += errorsMatch[0][1].toInteger()
                        }
                    } catch (Exception e) {
                        echo "Error reading file ${file}: ${e.getMessage()}"
                    }
                }
            }
            
            results = [
                total: testsTotal,
                failed: testsFailed,
                skipped: testsSkipped,
                errors: testsErrors,
                passed: testsTotal - testsFailed - testsSkipped - testsErrors
            ]
        }
    } catch (Exception e) {
        echo "Error reading test results: ${e.getMessage()}"
    }
    
    return results
}

/**
 * Отправляет уведомление в Telegram
 */
def sendTelegramNotification(String message) {
    try {
        def botToken = env.TELEGRAM_BOT_TOKEN
        def chatId = env.TELEGRAM_CHAT_ID
        
        // Используем curl для отправки сообщения
        def telegramUrl = "https://api.telegram.org/bot${botToken}/sendMessage"
        
        // Создаем временный файл для сообщения (избегаем проблем с экранированием)
        writeFile file: 'telegram_message.txt', text: message
        
        sh """
            curl -s -X POST "${telegramUrl}" \\
                -F "chat_id=${chatId}" \\
                -F "text=@telegram_message.txt" \\
                -F "parse_mode=Markdown" \\
                -F "disable_web_page_preview=true" || echo "Failed to send Telegram notification"
        """
        
        // Удаляем временный файл
        sh 'rm -f telegram_message.txt'
        
        echo '✅ Telegram notification sent'
    } catch (Exception e) {
        echo "❌ Error sending Telegram notification: ${e.getMessage()}"
    }
}

/**
 * Отправляет уведомление на Email
 */
def sendEmailNotification(String message, String status, Map params = [:]) {
    try {
        def emailTo = env.EMAIL_TO
        def emailFrom = env.EMAIL_FROM ?: 'jenkins@example.com'
        def subject = params.subject ?: "[${status}] ${env.JOB_NAME} - Build #${env.BUILD_NUMBER}"
        
        // Создаем временный файл для email
        writeFile file: 'email_message.txt', text: message
        
        sh """
            if command -v mail &> /dev/null; then
                mail -s "${subject}" -r "${emailFrom}" "${emailTo}" < email_message.txt || echo "Mail command failed"
            elif command -v sendmail &> /dev/null; then
                (
                    echo "To: ${emailTo}"
                    echo "From: ${emailFrom}"
                    echo "Subject: ${subject}"
                    echo "Content-Type: text/plain; charset=UTF-8"
                    echo ""
                    cat email_message.txt
                ) | sendmail "${emailTo}" || echo "Sendmail failed"
            else
                echo "No mail command available. Install mailutils or sendmail for email notifications."
            fi
        """
        
        // Удаляем временный файл
        sh 'rm -f email_message.txt'
        
        echo '✅ Email notification sent'
    } catch (Exception e) {
        echo "❌ Error sending email notification: ${e.getMessage()}"
    }
}
