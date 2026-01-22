# Как закоммитить изменения в Git

## 📋 Коммит Jenkinsfile

### Если Jenkinsfile был изменен:

```bash
# 1. Добавить Jenkinsfile в индекс
git add Jenkinsfile

# 2. Закоммитить с описанием
git commit -m "Update Jenkinsfile: add Maven setup and test execution"

# 3. Отправить в репозиторий
git push origin main
```

### Или одной командой (если нужно добавить все изменения):

```bash
# Добавить все измененные файлы
git add .

# Закоммитить
git commit -m "Update: Jenkinsfile, tests, and dependencies"

# Отправить
git push origin main
```

---

## 📋 Коммит всех текущих изменений

Судя по `git status`, у вас есть следующие изменения:

### 1. Удаленный файл:
- `src/test/java/TestPageAuthorization.java` (удален, заменен на `PageAuthorizationTest.java`)

### 2. Новые файлы (неотслеживаемые):
- `CHANGES_APPLIED.md`
- `CODE_REVIEW.md`
- `GIT_COMMIT_INSTRUCTIONS.md`
- `REVIEW_SUMMARY.md`
- `pom.xml_IMPROVED`
- `src/main/java/org/example/PageAuth_IMPROVED.java`
- `src/test/java/TestPageAuthorization_IMPROVED.java`

### 3. Измененные файлы (вероятно):
- `pom.xml`
- `src/test/java/PageAuthorizationTest.java` (новый)
- `src/test/java/BaseTest.java` (новый)
- `src/main/java/org/example/PageAuth.java`

---

## 🚀 Рекомендуемая последовательность коммитов

### Вариант 1: Один коммит со всеми изменениями

```bash
# Добавить все изменения
git add .

# Закоммитить
git commit -m "Fix: Add BaseTest, rename test class, update PageAuth, update dependencies

- Add BaseTest class for test inheritance
- Rename TestPageAuthorization to PageAuthorizationTest (Maven Surefire pattern)
- Fix WebDriverWait initialization in PageAuth
- Remove code duplication in PageAuth
- Add assertions to tests
- Update dependencies (Selenium, Log4j, WebDriverManager)
- Update pom.xml with proper Surefire configuration"

# Отправить
git push origin main
```

### Вариант 2: Разделить на логические коммиты

```bash
# 1. Коммит с критическими исправлениями
git add src/test/java/BaseTest.java
git add src/test/java/PageAuthorizationTest.java
git add src/main/java/org/example/PageAuth.java
git commit -m "Fix: Add BaseTest, fix PageAuth WebDriverWait initialization"

# 2. Коммит с обновлением зависимостей
git add pom.xml
git commit -m "Update: Dependencies (Selenium 4.27.0, Log4j 2.23.1, WebDriverManager 5.9.2)"

# 3. Коммит с документацией (опционально)
git add *.md
git commit -m "Docs: Add code review and setup documentation"

# Отправить все коммиты
git push origin main
```

---

## 🔍 Проверка перед коммитом

### Проверить, что будет закоммичено:

```bash
# Посмотреть статус
git status

# Посмотреть изменения в конкретном файле
git diff Jenkinsfile
git diff pom.xml

# Посмотреть, что будет добавлено
git diff --cached
```

---

## ⚠️ Важные файлы для коммита

**Обязательно закоммитить:**
- ✅ `src/test/java/BaseTest.java` - базовый класс (критично!)
- ✅ `src/test/java/PageAuthorizationTest.java` - тест (критично!)
- ✅ `src/main/java/org/example/PageAuth.java` - исправленный Page Object
- ✅ `pom.xml` - обновленные зависимости

**Опционально (можно не коммитить):**
- 📄 `*_IMPROVED.java` - примеры для справки
- 📄 `*.md` - документация

---

## 🎯 Быстрая команда (все изменения)

```bash
git add -A && git commit -m "Fix: Complete code review improvements" && git push origin main
```

---

## 📝 Примеры сообщений коммитов

### Для Jenkinsfile:
```
"Update Jenkinsfile: add Maven installation and test execution"
"Fix Jenkinsfile: setup Maven in Docker container"
"Improve Jenkinsfile: add better error handling"
```

### Для тестов:
```
"Fix: Rename test class to match Maven Surefire pattern"
"Add: BaseTest class for test inheritance"
"Fix: Add assertions to authorization test"
```

### Для зависимостей:
```
"Update: Dependencies to secure versions"
"Fix: Update Log4j to 2.23.1 (security fix)"
"Update: Selenium to 4.27.0"
```

---

**После коммита Jenkins автоматически запустит сборку (если настроен webhook)!**
