package org.example;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class PageAuth {
    //Добавляем вебдрайвер
    WebDriver driver;
    WebDriverWait wait;

    //Локатор формы авторизации
    private By formAuth = By.className("auth-form");

    //Локатор поля логин
    private By fieldLogin = By.id("username");

    //Локатор поля пароль
    private By fieldPassword = By.id("password");

    //Локатор кнопки "Войти"
    private By buttonAuth = By.className("auth-button");

    //Локатор кнопки "Регистрация
    private By buttonPageRegistration = By.xpath("//a[@href='/register']");

    public PageAuth(WebDriver driver) {
        this.driver=driver;
    }


    //Вспомогательный метод клика
    public void click(By locator) {
        driver.findElement(locator).click();
    }

    //Вспомогательный метод ввода
    public void sendKeys(By locator, String keys) {
        driver.findElement(locator).sendKeys(keys);
    }

    //Вспомогательный метод ожидания видимости элемента
    private WebElement waitingUntilVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    //Метод клика по полю Логин
    public void clickFieldLogin() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.visibilityOfElementLocated(formAuth));
        click(fieldLogin);
    }

    //Метод ввода в поле Логин
    public void sendFieldLogin(String login) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.elementToBeClickable(fieldLogin));
        sendKeys(fieldLogin, login);
    }

    //Метод клика по полю Пароль
    public void clickFieldPassword() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.visibilityOfElementLocated(formAuth));
        click(fieldPassword);
    }

    //Метод ввода пароля в поле пароль
    public void sendFieldPassword(String password) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.elementToBeClickable(fieldPassword));
        sendKeys(fieldPassword, password);
    }

    // Метод клика по кнопке "Войти"
    public void clickButtonAuth() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        waitingUntilVisible(buttonAuth);
        click(buttonAuth);
    }

    //Метод клика по кнопке "Регистрация"
    public void clickButtonRegistration(){
        WebDriverWait wait = new WebDriverWait(driver,Duration.ofSeconds(3));
        waitingUntilVisible(buttonPageRegistration);
        click(buttonPageRegistration);
    }
}



