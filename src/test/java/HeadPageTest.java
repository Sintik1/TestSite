import org.example.HeadPage;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class HeadPageTest extends BaseTest {

    private HeadPage headPage;
    private DatabaseHelper dbHelper;

    private static final String VALID_LOGIN = "testovik";
    private static final String VALID_PASSWORD = "123456";
    private static final String NAME_BLOCK = "test";
    private static final long DB_WAIT_TIMEOUT_MS = 7000;
    private static final long DB_WAIT_POLL_MS = 200;

    private boolean waitUntilTrue(long timeoutMs, long pollMs, java.util.function.BooleanSupplier condition) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            try {
                if (condition.getAsBoolean()) return true;
            } catch (Exception ignored) {
                // Игнорируем временные проблемы, пробуем ещё
            }
            try {
                Thread.sleep(pollMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    @Before
    public void initPage() {
        headPage = new HeadPage(driver);
        dbHelper = new DatabaseHelper();

        // Очищаем БД перед каждым тестом (удаляем тестовый блок, если существует)
        dbHelper.deleteGoal(NAME_BLOCK);
    }

    @After
    public void cleanup() {
        // Очищаем БД после каждого теста
        dbHelper.deleteGoal(NAME_BLOCK);
    }

    // Создание блока
    @Test
    public void createBlock() {
        // Убеждаемся, что блок еще не существует в БД
        Assert.assertFalse("Блок уже существует в БД перед созданием",
                dbHelper.goalExists(NAME_BLOCK));

        // Создаем блок через UI
        headPage
                .login(VALID_LOGIN, VALID_PASSWORD)
                .sendNameBlock(NAME_BLOCK)
                .clickButtonAddBlock();

        // Проверяем, что блок отображается в UI
        Assert.assertTrue("Цель не создана в UI", headPage.goalsBlockIsVisible());

        // Проверяем, что запись создана в БД (с ожиданием, т.к. запись может появляться не мгновенно)
        Assert.assertTrue(
                "Запись не создана в БД",
                waitUntilTrue(DB_WAIT_TIMEOUT_MS, DB_WAIT_POLL_MS, () -> dbHelper.goalExists(NAME_BLOCK))
        );
    }
    // Создание блока без названия
    @Test
    public void createBlockNotName(){
        headPage
                .login(VALID_LOGIN,VALID_PASSWORD)
                .clickButtonAddBlock();

        Assert.assertTrue("Блок отобразился", headPage.goalsBlockIsNotVisible());
    }
    //Удаление блока
    @Test
    public void deleteBlock(){
        // Создаем блок через UI
        headPage
                .login(VALID_LOGIN, VALID_PASSWORD)
                .sendNameBlock(NAME_BLOCK)
                .clickButtonAddBlock();

        // Проверяем, что блок создан в UI
        Assert.assertTrue("Блок не создан в UI", headPage.goalsBlockIsVisible());

        // Проверяем, что запись создана в БД (с ожиданием)
        Assert.assertTrue(
                "Запись не создана в БД перед удалением",
                waitUntilTrue(DB_WAIT_TIMEOUT_MS, DB_WAIT_POLL_MS, () -> dbHelper.goalExists(NAME_BLOCK))
        );

        // Удаляем блок через UI
        headPage.deleteBlock();

        // Проверяем, что блок не отображается в UI
        Assert.assertTrue("Блок отображается в UI после удаления", headPage.goalsBlockIsNotVisible());

        // Проверяем, что запись удалена из БД (с ожиданием)
        Assert.assertTrue(
                "Запись не удалена из БД",
                waitUntilTrue(DB_WAIT_TIMEOUT_MS, DB_WAIT_POLL_MS, () -> !dbHelper.goalExists(NAME_BLOCK))
        );
    }
}
