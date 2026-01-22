# Настройка уведомлений Telegram и Email в Jenkins Pipeline

## ✅ Что добавлено

В `Jenkinsfile` добавлена отправка результатов тестов:
- 📱 **Telegram Bot** - уведомления в Telegram
- 📧 **Email** - уведомления на почту

---

## 📱 Настройка Telegram Bot

### Шаг 1: Создать Telegram Bot

1. Откройте Telegram и найдите бота [@BotFather](https://t.me/BotFather)
2. Отправьте команду `/newbot`
3. Следуйте инструкциям и получите **Bot Token** (например: `123456789:ABCdefGHIjklMNOpqrsTUVwxyz`)

### Шаг 2: Получить Chat ID

1. Найдите бота [@userinfobot](https://t.me/userinfobot) в Telegram
2. Отправьте ему любое сообщение
3. Скопируйте ваш **Chat ID** (например: `123456789`)

Или создайте группу и добавьте бота, затем получите Chat ID группы.

### Шаг 3: Настроить в Jenkins

#### Вариант A: Через переменные окружения в Jenkinsfile

Отредактируйте `Jenkinsfile` и укажите значения напрямую:

```groovy
environment {
    TELEGRAM_BOT_TOKEN = 'YOUR_BOT_TOKEN_HERE'
    TELEGRAM_CHAT_ID = 'YOUR_CHAT_ID_HERE'
}
```

#### Вариант B: Через Jenkins Credentials (рекомендуется)

1. **Jenkins Dashboard** → **Manage Jenkins** → **Credentials**
2. **Add Credentials**:
   - **Kind**: Secret text
   - **Secret**: `YOUR_BOT_TOKEN`
   - **ID**: `telegram-bot-token`
   - **Description**: Telegram Bot Token
3. Повторите для Chat ID:
   - **Secret**: `YOUR_CHAT_ID`
   - **ID**: `telegram-chat-id`
   - **Description**: Telegram Chat ID

Затем в `Jenkinsfile` используйте:

```groovy
environment {
    TELEGRAM_BOT_TOKEN = credentials('telegram-bot-token')
    TELEGRAM_CHAT_ID = credentials('telegram-chat-id')
}
```

#### Вариант C: Через Jenkins Job Configuration

1. Откройте ваш Pipeline Job → **Configure**
2. В секции **Build Environment** → **Use secret text(s) or file(s)**
3. Добавьте:
   - **Variable**: `TELEGRAM_BOT_TOKEN`
   - **Secret**: `YOUR_BOT_TOKEN`
4. Повторите для `TELEGRAM_CHAT_ID`

---

## 📧 Настройка Email

### Шаг 1: Установить mail утилиты (если нет)

В Jenkins контейнере:

```bash
# Войдите в контейнер Jenkins
docker exec -it goals-jenkins bash

# Установите mailutils
apt-get update && apt-get install -y mailutils

# Или sendmail
apt-get install -y sendmail
```

Или добавьте в `docker-compose-microservices.yml`:

```yaml
jenkins:
  # ... существующие настройки
  volumes:
    # ... существующие volumes
  # Добавьте установку mailutils при старте
  command: >
    sh -c "
    apt-get update && apt-get install -y mailutils || true &&
    /usr/local/bin/jenkins.sh
    "
```

### Шаг 2: Настроить SMTP (если нужно)

Если используете внешний SMTP сервер (Gmail, Outlook и т.д.), настройте в Jenkins:

1. **Manage Jenkins** → **Configure System**
2. **E-mail Notification**:
   - **SMTP server**: `smtp.gmail.com` (для Gmail)
   - **SMTP Port**: `587`
   - **Credentials**: добавьте логин/пароль
   - **Use SSL**: ☑
   - **Use TLS**: ☑

### Шаг 3: Указать Email получателя

#### Вариант A: В Jenkinsfile

```groovy
environment {
    EMAIL_TO = 'your-email@example.com'
    EMAIL_FROM = 'jenkins@example.com'
}
```

#### Вариант B: Через Jenkins Credentials

1. **Credentials** → **Add Credentials**
   - **Kind**: Secret text
   - **Secret**: `your-email@example.com`
   - **ID**: `email-to`

В `Jenkinsfile`:

```groovy
environment {
    EMAIL_TO = credentials('email-to')
}
```

#### Вариант C: Через Job Configuration

1. **Configure** → **Build Environment** → **Use secret text(s) or file(s)**
2. Добавьте переменную `EMAIL_TO` с вашим email

---

## 🧪 Проверка работы

### Тест Telegram

1. Запустите Pipeline вручную
2. После завершения проверьте Telegram - должно прийти сообщение

### Тест Email

1. Запустите Pipeline вручную
2. Проверьте почтовый ящик (включая спам)

---

## 📋 Формат уведомлений

### Telegram сообщение:

```
✅ Test Pipeline - Build #42

Статус: Успешно

Результаты тестов:
• Всего: 1
• Успешно: 1
• Провалено: 0
• Ошибки: 0
• Пропущено: 0

Информация:
• Ветка: origin/main
• Коммит: abc1234
• Ссылка: http://jenkins:8080/job/Test/42/

Время: Mon Jan 22 05:30:00 UTC 2026
```

### Email сообщение:

То же самое, но в текстовом формате.

---

## ⚙️ Дополнительные настройки

### Отключить уведомления

Оставьте переменные пустыми:

```groovy
environment {
    TELEGRAM_BOT_TOKEN = ''
    TELEGRAM_CHAT_ID = ''
    EMAIL_TO = ''
}
```

### Отправлять только при ошибках

Измените в `Jenkinsfile` секцию `post`:

```groovy
post {
    failure {
        script {
            // Отправка уведомлений только при ошибке
            sendNotifications()
        }
    }
}
```

### Кастомное сообщение

Отредактируйте функцию `sendNotifications()` в `Jenkinsfile` для изменения формата сообщения.

---

## 🔒 Безопасность

⚠️ **Важно**: Не коммитьте токены и пароли в Git!

- Используйте Jenkins Credentials
- Или переменные окружения на уровне Jenkins
- Никогда не добавляйте секреты напрямую в Jenkinsfile в репозитории

---

## 🐛 Устранение проблем

### Telegram не отправляет сообщения

1. Проверьте, что `TELEGRAM_BOT_TOKEN` и `TELEGRAM_CHAT_ID` установлены
2. Проверьте логи Jenkins на наличие ошибок curl
3. Убедитесь, что Jenkins имеет доступ к интернету
4. Проверьте, что бот не заблокирован

### Email не отправляется

1. Проверьте, что `EMAIL_TO` установлен
2. Убедитесь, что установлен `mailutils` или `sendmail`
3. Проверьте логи Jenkins
4. Если используете внешний SMTP, проверьте настройки в Jenkins

### Ошибка "No mail command available"

Установите mailutils в Jenkins контейнере (см. Шаг 1 настройки Email).

---

## 📚 Полезные ссылки

- [Telegram Bot API](https://core.telegram.org/bots/api)
- [Jenkins Email Extension Plugin](https://plugins.jenkins.io/email-ext/)
- [Jenkins Credentials](https://www.jenkins.io/doc/book/using/using-credentials/)

---

**Готово! После настройки уведомления будут отправляться автоматически после каждого запуска тестов.**
