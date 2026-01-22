import io.github.bonigarcia.wdm.WebDriverManager;
import org.example.PageAuth;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.net.MalformedURLException;
import java.net.URL;

public class TestPageAuthorization {
    //Объявляем драйвер
    WebDriver driver;

    //Адресс страницы к которой драйвер будет подключаться для теста
    // Можно переопределить через переменную окружения BASE_URI или -DbaseUri
    public static final String BASE_URI = System.getProperty(
            "baseUri",
            System.getenv().getOrDefault("BASE_URI", "http://localhost:9090")
    );


    //Установка драйвера
    @Before
    public void setup(){

        ChromeOptions chromeOptions = buildChromeOptions();
        driver = createWebDriver(chromeOptions);

        //Вход на главную страницу
        driver.get(BASE_URI);
    }

    //Позитивный сценарий авторизации абонента
    @Test
    public void successAuthorization(){
        PageAuth pageAuth = new PageAuth(driver);
        pageAuth.clickFieldLogin();
        pageAuth.sendFieldLogin("testovik");
        pageAuth.clickFieldPassword();
        pageAuth.sendFieldPassword("123456");
    }

    //Метод закрытия браузера после прохождения теста
    @After
    public void tearDown(){
        if(driver!=null){
            driver.quit();
        }
    }

    private static ChromeOptions buildChromeOptions() {
        ChromeOptions options = new ChromeOptions();

        // Устойчивые флаги для CI/контейнеров (Jenkins, Docker)
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");

        // Иногда требуется для новых версий Chrome/driver в CI
        options.addArguments("--remote-allow-origins=*");

        // Позволяет явно указать путь до бинарника Chrome/Chromium в агенте Jenkins
        String chromeBinary = System.getenv("CHROME_BINARY");
        if (chromeBinary != null && !chromeBinary.isBlank()) {
            options.setBinary(chromeBinary);
        }

        return options;
    }

    /**
     * Если задан SELENIUM_REMOTE_URL — используем Selenium Grid (рекомендуется для Jenkins).
     * Иначе пробуем локальный ChromeDriver (требует установленного Chrome/Chromium на агенте).
     */
    private static WebDriver createWebDriver(ChromeOptions options) {
        boolean isCi = System.getenv("CI") != null || System.getenv("JENKINS_HOME") != null;
        String remoteUrl = System.getenv("SELENIUM_REMOTE_URL");
        if (remoteUrl != null && !remoteUrl.isBlank()) {
            try {
                System.out.println("[Test] Using RemoteWebDriver: " + remoteUrl);
                return new RemoteWebDriver(new URL(remoteUrl), options);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("Invalid SELENIUM_REMOTE_URL: " + remoteUrl, e);
            }
        }

        if (isCi) {
            throw new IllegalStateException(
                    "CI detected, but SELENIUM_REMOTE_URL is not set. " +
                    "Set SELENIUM_REMOTE_URL (e.g. http://selenium:4444/wd/hub) " +
                    "and BASE_URI (e.g. http://goals-app:8080) in Jenkins environment."
            );
        }

        // Локальный запуск (на Jenkins часто ломается, если в агенте нет Chrome/Chromium)
        System.out.println("[Test] Using local ChromeDriver");
        WebDriverManager.chromedriver().setup();
        return new ChromeDriver(options);
    }

}
