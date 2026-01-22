package org.example;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page Object для страницы авторизации.
 * Исправлено: инициализация WebDriverWait, убрано дублирование кода.
 */
public class PageAuth {
    private final WebDriver driver;
    private final WebDriverWait wait;

    // Локаторы
    private static final By FORM_AUTH = By.className("auth-form");
    private static final By FIELD_LOGIN = By.id("username");
    private static final By FIELD_PASSWORD = By.id("password");
    private static final By BUTTON_AUTH = By.className("auth-button");
    private static final By BUTTON_REGISTRATION = By.xpath("//a[@href='/register']");
    private static final By ERROR_AUTHORIZATION = By.className("error-message");
    // Таймаут по умолчанию
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);

    public PageAuth(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, DEFAULT_TIMEOUT);
    }

    // Вспомогательный метод клика с ожиданием
    private void click(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    // Вспомогательный метод ввода с ожиданием
    private void sendKeys(By locator, String keys) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
        element.clear();
        element.sendKeys(keys);
    }

    // Вспомогательный метод ожидания видимости элемента
    private WebElement waitForElementVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    // Метод проверки видимости формы авторизации (для Fluent Interface)
    public PageAuth verifyAuthFormVisible() {
        waitForElementVisible(FORM_AUTH);
        return this;
    }

    // Метод клика по полю Логин
    public PageAuth clickFieldLogin() {
        waitForElementVisible(FORM_AUTH);
        click(FIELD_LOGIN);
        return this;
    }

    // Метод ввода в поле Логин
    public PageAuth sendFieldLogin(String login) {
        sendKeys(FIELD_LOGIN, login);
        return this;
    }

    // Метод ввода логина (Fluent Interface)
    public PageAuth enterLogin(String login) {
        sendKeys(FIELD_LOGIN, login);
        return this;
    }

    // Метод клика по полю Пароль
    public PageAuth clickFieldPassword() {
        waitForElementVisible(FORM_AUTH);
        click(FIELD_PASSWORD);
        return this;
    }

    // Метод ввода пароля в поле пароль
    public PageAuth sendFieldPassword(String password) {
        sendKeys(FIELD_PASSWORD, password);
        return this;
    }

    // Метод ввода пароля (Fluent Interface)
    public PageAuth enterPassword(String password) {
        sendKeys(FIELD_PASSWORD, password);
        return this;
    }

    // Метод клика по кнопке "Войти"
    public PageAuth clickButtonAuth() {
        waitForElementVisible(BUTTON_AUTH);
        click(BUTTON_AUTH);
        return this;
    }

    // Метод клика по кнопке "Войти" (Fluent Interface)
    public PageAuth clickLoginButton() {
        waitForElementVisible(BUTTON_AUTH);
        click(BUTTON_AUTH);
        return this;
    }

    // Метод клика по кнопке "Регистрация"
    public PageAuth clickButtonRegistration() {
        waitForElementVisible(BUTTON_REGISTRATION);
        click(BUTTON_REGISTRATION);
        return this;
    }

    // Проверка видимости формы авторизации
    public boolean isAuthFormVisible() {
        try {
            waitForElementVisible(FORM_AUTH);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Проверка видимости поля логин
    public boolean isLoginFieldVisible() {
        try {
            waitForElementVisible(FIELD_LOGIN);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    // Метод получения текста ошибки при авторизации
    public String getTextError() {
        try {
            WebElement element = waitForElementVisible(ERROR_AUTHORIZATION);
            String actualText = element.getText();
            return actualText;
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}



