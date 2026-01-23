
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Базовый класс для всех тестов.
 * Содержит общую логику setup/teardown и управление драйвером.
 */
public abstract class BaseTest {
    
    protected WebDriver driver;
    
    protected static final String BASE_URI = System.getProperty(
            "baseUri",
            System.getenv().getOrDefault("BASE_URI", "http://localhost:9090")
    );
    
    @Before
    public void setUp() {
        ChromeOptions chromeOptions = buildChromeOptions();
        driver = createWebDriver(chromeOptions);
        driver.get(BASE_URI);
    }
    
    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
    
    /**
     * Создает ChromeOptions с оптимальными настройками для CI/CD
     */
    protected ChromeOptions buildChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        
        // Флаги для CI/контейнеров
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--remote-allow-origins=*");
        
        // Позволяет указать путь до Chrome binary
        String chromeBinary = System.getenv("CHROME_BINARY");
        if (chromeBinary != null && !chromeBinary.isBlank()) {
            options.setBinary(chromeBinary);
        }
        
        return options;
    }
    
    /**
     * Создает WebDriver: RemoteWebDriver для CI, ChromeDriver для локального запуска
     */
    protected WebDriver createWebDriver(ChromeOptions options) {
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
                    "and BASE_URI (e.g. http://frontend-service:9090) in Jenkins environment."
            );
        }
        
        System.out.println("[Test] Using local ChromeDriver");
        WebDriverManager.chromedriver().setup();
        return new ChromeDriver(options);
    }
}
