import org.example.HeadPage;
import org.example.PageAuth;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class HeadPageTest extends BaseTest {

    private HeadPage headPage;

    private static final String VALID_LOGIN = "testovik";
    private static final String VALID_PASSWORD = "123456";
    private static final String NAME_BLOCK = "test";

    @Before
    public void initPage() {
        headPage = new HeadPage(driver);
    }

    // Создание блока
    @Test
    public void createBlock() {
        headPage
                .login(VALID_LOGIN, VALID_PASSWORD)
                .sendNameBlock(NAME_BLOCK)
                .clickButtonAddBlock();

        Assert.assertTrue("Цель не создана", headPage.goalsBlockIsVisible());
    }
    // Создание блока без названия
    @Test
    public void createBlockNotName(){
        headPage
                .login(VALID_LOGIN,VALID_PASSWORD)
                .clickButtonAddBlock();

        Assert.assertFalse("Блок отобразился", headPage.goalsBlockIsNotVisible());
    }
    //Удаление блока
    @Test
    public void deleteBlock(){
        headPage
                .login(VALID_LOGIN, VALID_PASSWORD)
                .sendNameBlock(NAME_BLOCK)
                .clickButtonAddBlock()
                .deleteBlock();
        Assert.assertFalse("Блок отобразился", headPage.goalsBlockIsNotVisible());
    }
}
