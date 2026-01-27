# Настройка базы данных для тестов

## Описание

Тесты теперь проверяют не только UI, но и состояние базы данных:
- При создании блока цели проверяется, что запись создана в БД
- При удалении блока проверяется, что запись удалена из БД

## Настройка подключения к БД

### Переменные окружения

Настройте следующие переменные окружения для подключения к БД:

```bash
# URL подключения к БД (по умолчанию: jdbc:postgresql://localhost:5432/goals_db)
export DB_URL="jdbc:postgresql://localhost:5432/goals_db"

# Пользователь БД (по умолчанию: postgres)
export DB_USER="postgres"

# Пароль БД (по умолчанию: postgres)
export DB_PASSWORD="postgres"

# Имя таблицы (по умолчанию: categories)
export DB_TABLE="categories"

# Имя колонки с названием блока (по умолчанию: name)
export DB_NAME_COLUMN="name"

# Имя колонки с ID (по умолчанию: id)
export DB_ID_COLUMN="id"
```

### Настройка в Jenkins

Добавьте переменные окружения в Jenkins:

1. Откройте настройки проекта Jenkins
2. Перейдите в "Build Environment" или "Pipeline" -> "Environment variables"
3. Добавьте переменные:
   ```
   DB_URL=jdbc:postgresql://db-service:5432/goals_db
   DB_USER=postgres
   DB_PASSWORD=postgres
   DB_TABLE=categories
   DB_NAME_COLUMN=name
   DB_ID_COLUMN=id
   ```

### Настройка в docker-compose

Если БД запущена в Docker, убедитесь, что:
- БД доступна из контейнера Jenkins
- Используется правильный hostname (например, `db-service` вместо `localhost`)

Пример настройки в `docker-compose.yml`:
```yaml
services:
  jenkins:
    environment:
      - DB_URL=jdbc:postgresql://postgres:5432/goals_db
      - DB_USER=postgres
      - DB_PASSWORD=postgres
    depends_on:
      - postgres
  
  postgres:
    image: postgres:15
    environment:
      - POSTGRES_DB=goals_db
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=postgres
    ports:
      - "5432:5432"
```

## Структура таблицы

Ожидаемая структура таблицы `categories` (или указанной в `DB_TABLE`):

```sql
CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    -- другие колонки...
);
```

## Использование MySQL вместо PostgreSQL

Если используется MySQL, измените:

1. **pom.xml** - замените зависимость:
```xml
<!-- Вместо PostgreSQL -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
    <scope>test</scope>
</dependency>
```

2. **DB_URL** - измените на:
```bash
export DB_URL="jdbc:mysql://localhost:3306/goals_db"
```

3. **DatabaseHelper.java** - измените `DEFAULT_DB_URL`:
```java
private static final String DEFAULT_DB_URL = "jdbc:mysql://localhost:3306/goals_db";
```

## Проверка подключения

Для проверки подключения к БД можно использовать метод `testConnection()`:

```java
DatabaseHelper dbHelper = new DatabaseHelper();
if (dbHelper.testConnection()) {
    System.out.println("Подключение к БД успешно");
} else {
    System.out.println("Ошибка подключения к БД");
}
```

## Обработка ошибок

Если подключение к БД недоступно:
- Тесты не упадут с ошибкой
- Метод `goalExists()` вернет `false` при ошибке подключения
- Это позволяет запускать тесты даже без БД (только UI проверки)

Для строгой проверки БД убедитесь, что БД доступна перед запуском тестов.

## Примеры использования

### Локальный запуск

```bash
export DB_URL="jdbc:postgresql://localhost:5432/goals_db"
export DB_USER="postgres"
export DB_PASSWORD="mypassword"
mvn test
```

### Запуск в Jenkins

Переменные окружения должны быть настроены в Jenkins pipeline или в настройках проекта.

## Отладка

Если тесты не находят записи в БД:

1. Проверьте подключение: `dbHelper.testConnection()`
2. Проверьте правильность имени таблицы и колонок
3. Проверьте, что запись действительно создается (запрос в БД)
4. Убедитесь, что используется правильная БД (не production!)
