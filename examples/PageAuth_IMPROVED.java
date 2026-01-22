package org.example;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Улучшенная версия PageAuth с исправлениями:
 * 1. Инициализация WebDriverWait в конструкторе
 * 2. Единый экземпляр wait (убрано дублирование)
 * 3. Явные ожидания перед действиями
 * 4. Fluent interface методы
 * 5. Методы для проверки состояния страницы
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
    
    // Таймауты
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration SHORT_TIMEOUT = Duration.ofSeconds(5);
    
    public PageAuth(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, DEFAULT_TIMEOUT);
    }
    
    /**
     * Ожидает видимость элемента
     */
    private WebElement waitForElementVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
    
    /**
     * Ожидает кликабельность элемента
     */
    private WebElement waitForElementClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }
    
    /**
     * Проверяет, что форма авторизации видна
     */
    public PageAuth verifyAuthFormVisible() {
        waitForElementVisible(FORM_AUTH);
        return this;
    }
    
    /**
     * Кликает по полю логин (Fluent interface)
     */
    public PageAuth clickFieldLogin() {
        verifyAuthFormVisible();
        waitForElementClickable(FIELD_LOGIN).click();
        return this;
    }
    
    /**
     * Вводит логин (Fluent interface)
     */
    public PageAuth enterLogin(String login) {
        waitForElementClickable(FIELD_LOGIN).clear();
        waitForElementClickable(FIELD_LOGIN).sendKeys(login);
        return this;
    }
    
    /**
     * Кликает по полю пароль (Fluent interface)
     */
    public PageAuth clickFieldPassword() {
        verifyAuthFormVisible();
        waitForElementClickable(FIELD_PASSWORD).click();
        return this;
    }
    
    /**
     * Вводит пароль (Fluent interface)
     */
    public PageAuth enterPassword(String password) {
        waitForElementClickable(FIELD_PASSWORD).clear();
        waitForElementClickable(FIELD_PASSWORD).sendKeys(password);
        return this;
    }
    
    /**
     * Кликает по кнопке "Войти"
     */
    public PageAuth clickLoginButton() {
        waitForElementClickable(BUTTON_AUTH).click();
        return this;
    }
    
    /**
     * Кликает по кнопке "Регистрация"
     */
    public PageAuth clickRegistrationButton() {
        waitForElementClickable(BUTTON_REGISTRATION).click();
        return this;
    }
    
    /**
     * Выполняет полную авторизацию (Fluent interface)
     */
    public PageAuth login(String login, String password) {
        return clickFieldLogin()
                .enterLogin(login)
                .clickFieldPassword()
                .enterPassword(password)
                .clickLoginButton();
    }
    
    /**
     * Проверяет, что поле логин видно
     */
    public boolean isLoginFieldVisible() {
        try {
            waitForElementVisible(FIELD_LOGIN);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Проверяет, что поле пароль видно
     */
    public boolean isPasswordFieldVisible() {
        try {
            waitForElementVisible(FIELD_PASSWORD);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    // Старые методы для обратной совместимости (deprecated)
    @Deprecated
    public void sendFieldLogin(String login) {
        enterLogin(login);
    }
    
    @Deprecated
    public void sendFieldPassword(String password) {
        enterPassword(password);
    }
    
    @Deprecated
    public void clickButtonAuth() {
        clickLoginButton();
    }
    
    @Deprecated
    public void clickButtonRegistration() {
        clickRegistrationButton();
    }
}
