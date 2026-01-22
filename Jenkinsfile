pipeline {
    agent any
    
    environment {
        SELENIUM_REMOTE_URL = 'http://selenium:4444/wd/hub'
        BASE_URI = 'http://frontend-service:9090'
        CI = 'true'
        
        // Telegram Bot настройки (можно задать через Jenkins Credentials или переменные окружения)
        // Для использования credentials: credentials('telegram-bot-token')
        // Или напрямую: 'YOUR_BOT_TOKEN'
        TELEGRAM_BOT_TOKEN = '8263907755:AAG6nR_3bkV6ZEjD2Mhu3AdVT0kVYZsxsE0'
        TELEGRAM_CHAT_ID = '6284947582'
        
        // Email настройки
        EMAIL_TO = 'vsentyakov@yandex.ru'
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
                def testResults = [total: 0, failed: 0, skipped: 0, errors: 0, passed: 0]
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
                        
                        testResults = [
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
                
                // Формируем сообщение (используем простой текст без Markdown для надежности)
                def emoji = status == 'SUCCESS' ? '✅' : '❌'
                def statusText = status == 'SUCCESS' ? 'Успешно' : 'Провалено'
                def message = """
${emoji} ${jobName} - Build #${buildNumber}

Статус: ${statusText}

Результаты тестов:
• Всего: ${testResults.total}
• Успешно: ${testResults.passed}
• Провалено: ${testResults.failed}
• Ошибки: ${testResults.errors}
• Пропущено: ${testResults.skipped}

Информация:
• Ветка: ${gitBranch}
• Коммит: ${gitCommit.take(7)}
• Ссылка: ${buildUrl}

Время: ${new Date()}
""".trim()
                
                // Отправка в Telegram
                if (env.TELEGRAM_BOT_TOKEN && env.TELEGRAM_CHAT_ID && 
                    !env.TELEGRAM_BOT_TOKEN.isEmpty() && !env.TELEGRAM_CHAT_ID.isEmpty()) {
                    try {
                        def botToken = env.TELEGRAM_BOT_TOKEN
                        def chatId = env.TELEGRAM_CHAT_ID
                        def telegramUrl = "https://api.telegram.org/bot${botToken}/sendMessage"
                        
                        // Сохраняем сообщение в файл
                        writeFile file: 'telegram_message.txt', text: message
                        
                        // Отправляем через curl (используем переменную chatId из окружения)
                        def response = sh(
                            script: """
                                curl -s -X POST "${telegramUrl}" \\
                                    -d "chat_id=${env.TELEGRAM_CHAT_ID}" \\
                                    --data-urlencode "text@telegram_message.txt" \\
                                    -d "disable_web_page_preview=true"
                            """,
                            returnStdout: true
                        ).trim()
                        
                        sh 'rm -f telegram_message.txt'
                        
                        // Проверяем ответ от Telegram API
                        if (response.contains('"ok":true')) {
                            echo '✅ Telegram notification sent successfully'
                        } else if (response.contains('"error_code":403')) {
                            echo '❌ Telegram error: Chat ID is incorrect. Make sure you are using your USER Chat ID, not bot ID.'
                            echo "Response: ${response}"
                        } else {
                            echo "⚠️ Telegram response: ${response}"
                        }
                    } catch (Exception e) {
                        echo "❌ Error sending Telegram notification: ${e.getMessage()}"
                    }
                } else {
                    echo '⚠️ Telegram credentials not configured. Set TELEGRAM_BOT_TOKEN and TELEGRAM_CHAT_ID.'
                }
                
                // Отправка Email через Jenkins Mail Plugin
                if (env.EMAIL_TO && !env.EMAIL_TO.isEmpty()) {
                    try {
                        def emailTo = env.EMAIL_TO
                        def emailFrom = env.EMAIL_FROM ?: 'jenkins@example.com'
                        def subject = "[${status}] ${jobName} - Build #${buildNumber}"
                        
                        // Используем встроенный Jenkins mail step
                        mail(
                            to: emailTo,
                            from: emailFrom,
                            subject: subject,
                            body: message,
                            mimeType: 'text/plain'
                        )
                        
                        echo '✅ Email notification sent via Jenkins Mail Plugin'
                    } catch (Exception e) {
                        echo "❌ Error sending email notification: ${e.getMessage()}"
                        echo "💡 Tip: Configure SMTP in Jenkins (Manage Jenkins -> Configure System -> E-mail Notification)"
                        
                        // Fallback: пробуем через mail команду, если доступна
                        try {
                            def fallbackSubject = "[${status}] ${jobName} - Build #${buildNumber}"
                            def fallbackEmailTo = env.EMAIL_TO
                            def fallbackEmailFrom = env.EMAIL_FROM ?: 'jenkins@example.com'
                            
                            writeFile file: 'email_message.txt', text: message
                            sh """
                                if command -v mail &> /dev/null; then
                                    mail -s "${fallbackSubject}" -r "${fallbackEmailFrom}" "${fallbackEmailTo}" < email_message.txt || echo "Mail command failed"
                                elif command -v sendmail &> /dev/null; then
                                    (
                                        echo "To: ${fallbackEmailTo}"
                                        echo "From: ${fallbackEmailFrom}"
                                        echo "Subject: ${fallbackSubject}"
                                        echo "Content-Type: text/plain; charset=UTF-8"
                                        echo ""
                                        cat email_message.txt
                                    ) | sendmail "${fallbackEmailTo}" || echo "Sendmail failed"
                                else
                                    echo "No mail command available. Install mailutils or configure Jenkins SMTP."
                                fi
                            """
                            sh 'rm -f email_message.txt'
                        } catch (Exception e2) {
                            echo "Fallback email method also failed: ${e2.getMessage()}"
                        }
                    }
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
