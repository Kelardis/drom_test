package ru.drom.auto;

import okhttp3.HttpUrl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import ru.drom.auto.model.AdvItem;
import ru.drom.auto.utils.TestConfig;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class AutoDromTest {
    private ChromeDriver driver;
    private WebDriverWait wait;

    @Before
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("disable-logging");
        System.setProperty("webdriver.chrome.driver", TestConfig.getInstance().getChromeWebDriverLocation());
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, 3000);
    }

    @After
    public void tearDown() {
        driver.close();
        driver.quit();
    }

    /**
     * Дана страница продаж авто в России http://auto.drom.ru/
     * Отфильтровать список объявлений по параметрам:
     * - фирма автомобиля Toyota;
     * - марка автомобиля Harrier;
     * - гибрид;
     * - непроданные;
     * - пробег авто больше 1 км;
     * - год выпуска от 2007.
     * Проверять, что на первой и второй страницах результатов поиска в списке
     * объявлений:
     * - нет проданных авто (отсутствует перечеркнутый текст);
     * - год авто не меньше 2007;
     * - у каждого объявления в списке есть пробег.
     */
    @Test
    public void compoundFilterTest1() {
        ArrayList<AdvItem> itemList = new ArrayList<>();

        for (int i = 1; i <= 2; i++) {
            //собираем ссылку с фильтром для первой и второй страницы
            URL url = new HttpUrl.Builder()
                    .scheme("https")
                    .host("auto.drom.ru")
                    .addPathSegment("toyota")
                    .addPathSegment("harrier")
                    .addPathSegment("page" + String.valueOf(i))
                    .addPathSegment("")
                    .addQueryParameter("unsold", "1")
                    .addQueryParameter("minyear", "2007")
                    .addQueryParameter("minprobeg", "1")
                    .addQueryParameter("fueltype", "5")//hybrid=1 ??
                    .build().url();

            driver.get(url.toString());

            //извлекаем и парсим обьявления с первой и второй страницы
            for (WebElement element : driver.findElements(By.xpath("//a[contains(@class, 'b-advItem')]"))) {
                itemList.add(new AdvItem(element));
            }
        }


        Assert.assertFalse("No elements found", itemList.isEmpty());
        for (AdvItem item : itemList) {
            Assert.assertFalse("Found removed item", item.isRemoved());
            Assert.assertTrue("Found year lesser than minimal", item.getYear() >= 2007);
            Assert.assertNotNull("Mileage is absent", item.getMileage());
        }
    }

    /**
     * Написать тест, который авторизует пользователя в разделе продаж авто
     * http://auto.drom.ru/ и добавляет объявление о продаже авто в избранное.
     */
    @Test
    public void authorizeUserAndAddToFavorite() {

        //заходим на заглавную и кликаем по линку на вход
        driver.get("http://auto.drom.ru/");
        driver.get(driver.findElement(By.xpath("//div[@class='b-auth']/a[@class='b-auth__auth-link b-link']")).getAttribute("href"));

        //вводим логин/пароль и жмем кнопку обычного входа с паролем
        driver.findElementByName("sign").sendKeys(TestConfig.getInstance().getDromUserLogin());
        driver.findElementByName("password").sendKeys(TestConfig.getInstance().getDromUserPassword());
        driver.findElementById("signbutton").click();

        //переходим по ссылке на первое попавшееся в спецразмещении обьявление
        driver.get(driver.findElement(By.xpath("//div[@class='owl-item active']/a")).getAttribute("href"));

        //кликнуть по звездочке "добавить в избранное"
        driver.findElement(By.xpath("//div[contains(@class, 'favorite-star')]")).click();

        //и еще разок, что бы потом не делать это руками
        wait.until(f -> f.findElement(By.xpath("//div[contains(@class, 'favorite-star_active')]"))).click();

        //ничего не взорвалось? тест пройден
    }

    /**
     * Дана страница http://auto.drom.ru/
     * В сером блоке фильтрации по объявлениям есть выпадающие списки "Фирма" и
     * "Модель". В скобках напротив каждого пункта выпадающего списка указано
     * количество объявлений по данной фирме или модели.
     * Необходимо написать скрипт, который выводит список из 20 фирм с
     * наибольшим количеством поданных объявлений в Приморском крае. Данные
     * вывести в виде таблицы с двумя столбцами как указано на примере ниже.
     * | Фирма  | Количество объявлений |
     * | Toyota | 17211                 |
     */
    @Test
    public void top20firmsTable() {
        //заходим на приморский раздел auto.drom.ru
        driver.get("http://auto.drom.ru/region25/");

        HashMap<String, Integer> firms = new HashMap<>();

        //извлекаем из фильтра названия фирм и кол-во объявлений
        driver.findElements(By.xpath("//form[@name='filters']//select[@data-ftid='sales__filter_fid']/option[@value]"))
                .forEach(webElement -> {
                    String[] arr = webElement.getAttribute("innerHTML").split(" \\(");
                    //если есть число в скобках, то берем его, иначе объявлений 0
                    firms.put(arr[0], arr.length > 1 ? Integer.valueOf(arr[1].substring(0, arr[1].length() - 1)) : 0);
                });


        String format = "| %-20s | %-21s |\n";
        System.out.printf(format, "Фирма", "Количество объявлений");
        //выводим первые 20 с наибольщим кол-вом объявлений
        firms.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, Integer>>comparingInt(Map.Entry::getValue).reversed())
                .limit(20)
                .forEach(entry -> System.out.printf(format, entry.getKey(), entry.getValue()));
    }


}
