package org.example;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class HeadPage {

    //Объявяем драйверы
    private final WebDriver driver;
    private final WebDriverWait wait;

    private static final String VALID_LOGIN = "testovik";
    private static final String VALID_PASSWORD = "123456";

    //Локаторы
    private static final By BUTTON_ADD_BLOCK = By.className("primary-button");
    private static final By BUTTON_LOGOUT = By.className("logout-button");
    private static final By FIELD_NAME_BLOCK = By.id("category-name");
    private static final By TABLE_GOALS = By.className("goals-table");
    private static final By DELETE_BLOCK = By.className("category-delete-x-button");
    private static final By BLOCK_GOALS = By.xpath("//a[@href='/categories/1']");


    //Таймаут по умолчанию
    private static final Duration DEAFAULT_TIMEOUT = Duration.ofSeconds(10);

//Создаем конструктор класса
    public HeadPage(WebDriver driver) {
        this.driver = driver;
        this.wait =  new WebDriverWait(driver,DEAFAULT_TIMEOUT);
    }

    //Вспомогательный метод ожидания кликабельности
    public WebElement waitForClicableElement(By locator){
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    //Вспомогательный метод ожидания видимости
    public WebElement waitForInVisible(By locator){
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }


    //Вспомогательный метод авторизации
    public HeadPage login(String login,String password){
        PageAuth pageAuth = new PageAuth(driver);
        pageAuth.clickFieldLogin();
        pageAuth.sendFieldLogin(VALID_LOGIN);
        pageAuth.clickFieldPassword();
        pageAuth.sendFieldPassword(VALID_PASSWORD);
        pageAuth.clickButtonAuth();
        return this;
    }

        //Метод проверки что таблица целей видна
    public boolean verifyVisibleTableGoals(){
        waitForInVisible(TABLE_GOALS);
        return true;
    }

    //Клик по кнопке "Добавить блок
    public HeadPage clickButtonAddBlock(){
        waitForClicableElement(BUTTON_ADD_BLOCK).click();
        return this;
    }

    //Заполнение поля название блока
    public HeadPage sendNameBlock(String name){
        waitForClicableElement(FIELD_NAME_BLOCK).clear();
        waitForClicableElement(FIELD_NAME_BLOCK).sendKeys(name);
        return this;
    }
    // Метод создания блока
    public HeadPage createBlock(String name){
        HeadPage headPage = new HeadPage(driver);
        headPage
                .clickButtonAddBlock()
                .sendNameBlock(name);
        return  this;
    }
    //Метод удаления блока
    public HeadPage deleteBlock(){
        waitForInVisible(DELETE_BLOCK);
        waitForClicableElement(DELETE_BLOCK).click();
        return this;
    }
    //Метод выхода из Учетной записи
    public HeadPage logout(){
        waitForInVisible(BUTTON_LOGOUT);
        waitForClicableElement(BUTTON_LOGOUT).click();
        return this;
    }

    //метод отображения блока Цели
    public boolean goalsBlockIsVisible(){
        waitForInVisible(BLOCK_GOALS);
        return true;
    }

}
