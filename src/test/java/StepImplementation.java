import com.thoughtworks.gauge.Step;
import com.thoughtworks.gauge.Table;
import com.thoughtworks.gauge.TableRow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class StepImplementation extends BaseTest {

    private static final Logger logger = LogManager.getLogger(StepImplementation.class);

    public WebElement findElement(String locatorKey) {
        WebElement element = null;

        try {
            String jsonFilePath = "src/test/resources/locators.json";

            String jsonContent = new String(Files.readAllBytes(Paths.get(jsonFilePath)), StandardCharsets.UTF_8);

            JSONArray jsonArray = new JSONArray(jsonContent);

            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject locatorInfo = jsonArray.getJSONObject(i);

                String key = locatorInfo.getString("key");
                String method = locatorInfo.getString("type");
                String value = locatorInfo.getString("value");

                if (key.equals(locatorKey)) {
                    switch (method) {
                        case "id":
                            element = driver.findElement(By.id(value));
                            break;
                        case "css":
                            element = driver.findElement(By.cssSelector(value));
                            break;
                        case "xpath":
                            element = driver.findElement(By.xpath(value));
                            break;
                        default:
                            Assertions.fail("Locator tipi mevcut secenekler ile eslesmedi!" +
                                    "\nType: " + method);
                    }

                    if (element != null) {
                        break;
                    }
                }
            }

        } catch (JSONException | NoSuchElementException | IOException e) {
            Assertions.fail("Bir hata olustu: " + e.getMessage());
        }

        if (element == null) {
            Assertions.fail(locatorKey + " elementi bulunamadi!");
        }

        return element;
    }

    public WebElement findElementWithoutAssert(String key) {
        WebElement webElement = null;
        try {
            webElement = findElement(key);
        } catch (Exception ignored) {
        }
        return webElement;
    }

    public By findElementInfoBy(String locatorKey) {
        By by = null;
        boolean elementFound = false;

        try {
            String jsonFilePath = "src/test/resources/locators.json";
            String jsonContent = new String(Files.readAllBytes(Paths.get(jsonFilePath)), StandardCharsets.UTF_8);

            JSONArray jsonArray = new JSONArray(jsonContent);

            for (int index = 0; index < jsonArray.length(); index++) {

                JSONObject locatorInfo = jsonArray.getJSONObject(index);

                String key = locatorInfo.getString("key");
                String method = locatorInfo.getString("type");
                String value = locatorInfo.getString("value");

                if (key.equals(locatorKey)) {
                    switch (method) {
                        case "css":
                            by = By.cssSelector(value);
                            break;
                        case "id":
                            by = By.id(value);
                            break;
                        case "xpath":
                            by = By.xpath(value);
                            break;
                        default:
                            Assertions.fail("Locator tipi mevcut secenekler ile eslesmedi!" +
                                    "\nType: " + method);
                    }
                    elementFound = true;
                    break;
                }
            }

            if (!elementFound) {
                Assertions.fail("Element bulunamadi!");
            }
        } catch (JSONException | NoSuchElementException | IOException e) {
            Assertions.fail("Bir hata olustu: " + e.getMessage());
        }

        return by;
    }

    public List<WebElement> findElements(String key) {
        return driver.findElements(findElementInfoBy(key));
    }

    public boolean isElementVisible(String key, long timeout) {

        WebDriverWait wait = new WebDriverWait(driver, timeout);

        try {
            wait.until(ExpectedConditions.visibilityOf(findElement(key)));
        } catch (Exception e) {
            logger.info("false - " + key);
            return false;
        }
        logger.info("true - " + key + ": visible");
        return true;
    }

    public WebElement waitUntilElementToBeVisible(String key, long timeout) {
        WebDriverWait dynamicWait = new WebDriverWait(driver, timeout);

        logger.info(key + " elementi gorunur olana kadar bekleniyor...");
        return dynamicWait.until(ExpectedConditions.visibilityOf(findElement(key)));
    }

    public WebElement waitUntilElementToBeClickable(String key, long timeout) {
        WebDriverWait dynamicWait = new WebDriverWait(driver, timeout);

        logger.info(key + " elementi tiklanabilir olana kadar bekleniyor...");
        return dynamicWait.until(ExpectedConditions.elementToBeClickable(findElement(key)));
    }

    @Step("<key> elementi kaybolana kadar bekle")
    public void waitForElementToDisappear(String key) {

        WebDriverWait wait = new WebDriverWait(driver, 20);

        WebElement element = findElementWithoutAssert(key);

        if (element != null) {
            wait.until(ExpectedConditions.invisibilityOf(element));
            logger.info(key + " elementinin ekranda kaybolmasi beklendi");
        } else {
            logger.info(key + " elementi gorunur olmadi, devam ediliyor...");
        }
    }

    @Step("Bekle: <seconds> saniye")
    public void waitBySeconds(int seconds) {

        try {
            Thread.sleep(seconds * 1000L);
            logger.info(seconds + " saniye beklendi");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Step("Su anki URL <expectedURL> degerine esit mi kontrol et")
    public void checkCurrentURLEquals(String expectedURL) {

        String currentURL = driver.getCurrentUrl();
        if (currentURL.equals(expectedURL)) {
            logger.info("Mevcut URL belirtilen '" + expectedURL + "' degerine esit!");
        } else {
            Assertions.fail("Mevcut URL belirtilen '" + expectedURL + "' degerine esit degil!"
                    + "\nBeklenen URL: " + expectedURL + "\nMevcut URL: " + currentURL);
        }
    }

    @Step("<key> elementine tikla")
    public void clickElement(String key) {
        WebElement element = waitUntilElementToBeClickable(key, 10);
        element.click();
        logger.info(key + " elementine tiklandi");
    }

    @Step("<key> elementlerinden sonuncusuna tikla")
    public void clickLastElement(String key) {
        List<WebElement> webElementList = findElements(key);
        webElementList.get(webElementList.size() - 1).click();
        logger.info(key + " elementlerinden " + (webElementList.size()) + ".elemente tiklandi");
    }

    @Step("<key> elementine <text> degerini yaz")
    public void sendKeys(String key, String text) {
        WebElement element = waitUntilElementToBeVisible(key, 10);
        element.clear();
        element.sendKeys(text);
        logger.info(key + " elementi temizlendi ve '" + text + "' degeri gonderildi");
    }

    @Step("<key> elementine ENTER keyi yolla")
    public void sendKeyToElementENTER(String key) {
        findElement(key).sendKeys(Keys.ENTER);
        logger.info(key + " elementine ENTER keyi yollandi");
    }

    @Step("<key> elementinin degeri <expectedValue> degerine esit olana kadar <key2> elementine tikla")
    public void clickElementUntilValueIsExpected(String key, String expectedValue, String key2) {

        WebElement element1 = findElement(key);

        while (!element1.getText().equals(expectedValue)) {
            logger.info(key + " elementinin degeri: " + element1.getText() +
                    "\nBeklenen deger: " + expectedValue);
            clickElement(key2);

        }

        checkElementText(key, expectedValue);
    }

    @Step("<key> elementinin oldugu dogrulanir")
    public void confirmIfElementFound(String key) {
        Assertions.assertTrue(isElementVisible(key, 10),
                "Elementin oldugu dogrulanamadi");
        logger.info(key + " elementinin sayfada oldugu dogrulandi!");
    }

    @Step("<key> elementlerinin oldugu dogrulanir")
    public void confirmIfElementsFoundAndShowElementCount(String key) {
        Assertions.assertTrue(isElementVisible(key, 10),
                "Elementin oldugu dogrulanamadi");
        logger.info(key + " elementlerinin sayfada oldugu dogrulandi!");
        logger.info(key + " keyli elementlerin sayisi: " + findElements(key).size());
    }

    @Step("<key> elementinin text iceriginin <expectedText> oldugu dogrulanir")
    public void checkElementText(String key, String expectedText) {
        String elementText = waitUntilElementToBeVisible(key, 10).getText();
        boolean textEquality = elementText.equals(expectedText);
        Assertions.assertTrue(textEquality, key + " elementinin text icerigi beklenen degere esit degil!\n" +
                key + " elementinin text icerigi:" + elementText + "\n" +
                "expectedText:" + expectedText);

        logger.info(key + " elementinin text icerigi beklenen degere esit\n" +
                "elementText:" + elementText + "\t" +
                "expectedText:" + expectedText);
    }

    @Step("Tablodaki verilerle <key1> ve <key2> elementlerine parametrik veri girisi yapilir: <data1> <data2>")
    public void performParameterizedDataEntry(String key1, String key2, String data1, String data2) {

        sendKeys(key1,data1);
        waitBySeconds(1);
        sendKeyToElementENTER(key1);

        sendKeys(key2,data2);
        waitBySeconds(1);
        sendKeyToElementENTER(key2);

    }

}
