# Удаление файлов *_IMPROVED.java из Git

## Проблема

Файлы `*_IMPROVED.java` все еще находятся в Git репозитории, хотя они перемещены локально в `examples/`. Jenkins получает файлы из Git, поэтому пытается их скомпилировать и падает с ошибками.

## Решение

Нужно удалить эти файлы из Git репозитория:

```bash
# 1. Удалить файлы из Git (но оставить локально)
git rm --cached src/test/java/TestPageAuthorization_IMPROVED.java
git rm --cached src/main/java/org/example/PageAuth_IMPROVED.java

# 2. Закоммитить удаление
git commit -m "Remove IMPROVED example files from Git (moved to examples/)"

# 3. Отправить изменения
git push origin main
```

## Альтернатива: удалить полностью

Если файлы больше не нужны:

```bash
# Удалить из Git и локально
git rm src/test/java/TestPageAuthorization_IMPROVED.java
git rm src/main/java/org/example/PageAuth_IMPROVED.java

# Закоммитить
git commit -m "Remove IMPROVED example files"

# Отправить
git push origin main
```

## Проверка

После удаления проверьте:

```bash
# Проверить, что файлы удалены из Git
git ls-files | grep IMPROVED

# Должно быть пусто (если файлы удалены)
```

## Важно

- Файлы в `examples/` не будут компилироваться (они вне стандартных директорий Maven)
- `.gitignore` добавлен, чтобы предотвратить случайное добавление таких файлов в будущем
- После удаления из Git Jenkins перестанет их компилировать
