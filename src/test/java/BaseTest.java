import com.thoughtworks.gauge.AfterScenario;
import com.thoughtworks.gauge.BeforeScenario;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.concurrent.TimeUnit;

public class BaseTest {
    protected static WebDriver driver;
    private static final String BASE_URL = "https://www.enuygun.com/";


    @BeforeScenario
    public void setUp() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions chromeOptions = new ChromeOptions();
        DesiredCapabilities capabilities = DesiredCapabilities.chrome();

        chromeOptions.addArguments("--incognito");
        chromeOptions.addArguments("--disable-notifications");
        chromeOptions.addArguments("--start-fullscreen");
        chromeOptions.addArguments("--start-maximized");

        chromeOptions.merge(capabilities);

        driver = new ChromeDriver(chromeOptions);
        driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
        driver.get(BASE_URL);
    }

    @AfterScenario
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
