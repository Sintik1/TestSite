package org.example;

import org.openqa.selenium.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Iterator;
import java.util.Set;

import static org.apache.commons.exec.util.DebugUtils.handleException;


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
    private static final By DELETE_BLOCK = By.xpath("//button[contains(@class, 'category-delete-x-button') and normalize-space()='×']");
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

    //проверка что блок не отобажается
    public boolean goalsBlockIsNotVisible(){
        try {
            // Используем findElements, чтобы избежать исключения, если элемент не найден
            java.util.List<WebElement> elements = driver.findElements(BLOCK_GOALS);
            if (elements.isEmpty()) {
                // Элемент не найден - блок не отображается
                return true;
            }
            // Элемент найден - проверяем, отображается ли он
            return !elements.get(0).isDisplayed();
        } catch (Exception e) {
            // В случае любой ошибки считаем, что блок не отображается
            return true;
        }
    }

    // Отображение модального окна
    public boolean setWindowInList(String selectorChildWindow) {
        //Записали индентификатор главного окна
        String mainWindowHandle = driver.getWindowHandle();
        //Записали идентификаторы всех окон включая дочерние
        Set<String> allWindowHandles = driver.getWindowHandles();
        //Cоздаем итератор для перебора всех окон
        Iterator<String> iterator = allWindowHandles.iterator();

        // Здесь мы проверим, есть ли у дочернего окна другие дочерние окна, и проверим отображение  дочернего окна
        while (iterator.hasNext()) {
            String childWindow = iterator.next();
            if (!mainWindowHandle.equalsIgnoreCase(childWindow)) {
                driver.switchTo().window(childWindow);
                WebDriverWait wait = new WebDriverWait(driver, DEAFAULT_TIMEOUT);
                try {
                    WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(selectorChildWindow)));
                    return element.isDisplayed();
                } catch (Exception e) {
                    handleException("Ошибка",e);
                }
            }
        }
        //если окно не содержит элемент
        return false;
    }
    //Удаление блока цели
    public HeadPage deleteBlock() {
        WebElement blockElement = waitForInVisible(BLOCK_GOALS);
        // Пробуем навести на блок, чтобы кнопка удаления появилась (если она скрыта до наведения)
        Actions actions = new Actions(driver);
        actions.moveToElement(blockElement).perform();
        // Ждем появления и кликабельности кнопки удаления
        waitForClicableElement(DELETE_BLOCK).click();
        Alert alert = driver.switchTo().alert();
        alert.accept();
        return this;
    }

}
