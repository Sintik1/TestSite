import org.example.HeadPage;
import org.example.PageAuth;
import org.junit.Assert;
import org.junit.Test;

public class HeadPageTest extends BaseTest{
    HeadPage headPage = new HeadPage(driver);

    private static final String VALID_LOGIN = "testovik";
    private static final String VALID_PASSWORD = "123456";
    private static final String NAME_BLOCK ="test";

    //Создание блока
    @Test
    public void CreateBlock(){
        headPage
                .login(VALID_LOGIN,VALID_PASSWORD)
                .sendNameBlock(NAME_BLOCK)
                .clickButtonAddBlock();

        Assert.assertTrue("Цель не создана", headPage.goalsBlockIsVisible());


    }
}
