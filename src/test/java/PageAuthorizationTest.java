
import org.example.HeadPage;
import org.example.PageAuth;
import org.junit.Assert;
import org.junit.Test;

/**
 * Тесты авторизации.
 * Исправлено: добавлены assertions, используется BaseTest для переиспользования кода.
 */
public class PageAuthorizationTest extends BaseTest {

    // Тестовые данные
    private static final String VALID_LOGIN = "testovik";
    private static final String VALID_PASSWORD = "123456";
    private static final String INVALID_LOGIN = "invalid_user";
    private static final String INVALID_PASSWORD = "wrong_password";

    /**
     * Позитивный сценарий авторизации абонента.
     * Проверяет, что после ввода валидных данных форма авторизации обрабатывается корректно.
     */
    @Test
    public void successAuthorization() {
        PageAuth pageAuth = new PageAuth(driver);
        HeadPage headPage = new HeadPage(driver);


        // Выполняем действия авторизации
        pageAuth.clickFieldLogin();
        pageAuth.sendFieldLogin(VALID_LOGIN);
        pageAuth.clickFieldPassword();
        pageAuth.sendFieldPassword(VALID_PASSWORD);
        pageAuth.clickButtonAuth();

        //проверяем что Главная страница отобразилась
        Assert.assertTrue("Авторизация прошла не успешно, возникла ошибка", headPage.verifyVisibleTableGoals());



        // Временная проверка: убеждаемся, что драйвер работает и страница загрузилась
        Assert.assertNotNull("Driver should be initialized", driver);
        Assert.assertNotNull("Page title should not be null", driver.getTitle());
        
        // Проверка: если форма авторизации все еще видна после клика на кнопку,
        // это может означать ошибку авторизации (неверные данные)
        // Раскомментируйте следующую строку, если нужно проверить, что форма исчезла:
        // Assert.assertFalse("Auth form should disappear after successful login", 
        //     pageAuth.isAuthFormVisible());
    }
     /**
     * Негативный тест: авторизация с неверным логином
     */
    @Test
    public void testAuthorizationWithInvalidLogin() {
        PageAuth pageAuth = new PageAuth(driver);
        
        pageAuth
                .verifyAuthFormVisible()
                .clickFieldLogin()
                .enterLogin(INVALID_LOGIN)
                .clickFieldPassword()
                .enterPassword(VALID_PASSWORD)
                .clickLoginButton();
        
        // Проверяем, что форма авторизации все еще видна (ошибка)
        // TODO: Добавить проверку сообщения об ошибке
        Assert.assertTrue("Поле авторизации должно оставаться видимым даже после неудачной попытки входа в систему", 
                pageAuth.isLoginFieldVisible());
         Assert.assertEquals("Данные неверные. Попробуйте ещё раз" ,pageAuth.getTextError());
    }
    
    /**
     * Негативный тест: авторизация с неверным паролем
     */
    @Test
    public void testAuthorizationWithInvalidPassword() {
        PageAuth pageAuth = new PageAuth(driver);
        
        pageAuth
                .verifyAuthFormVisible()
                .clickFieldLogin()
                .enterLogin(VALID_LOGIN)
                .clickFieldPassword()
                .enterPassword(INVALID_PASSWORD)
                .clickLoginButton();
        
        // Проверяем, что форма авторизации все еще видна
        Assert.assertTrue("Login field should still be visible after failed login", 
                pageAuth.isLoginFieldVisible());
        Assert.assertEquals("Данные неверные. Попробуйте ещё раз" ,pageAuth.getTextError());
    }
    
    /**
     * Негативный тест: авторизация с пустыми полями
     */
    @Test
    public void testAuthorizationWithEmptyFields() {
        PageAuth pageAuth = new PageAuth(driver);
        
        pageAuth
                .verifyAuthFormVisible()
                .clickLoginButton(); // Пытаемся войти без данных
        
        // Проверяем, что форма все еще видна
        Assert.assertTrue("Login field should still be visible", 
                pageAuth.isLoginFieldVisible());
    }
}
