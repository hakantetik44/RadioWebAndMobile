package stepdefinitions;

import io.appium.java_client.android.AndroidDriver;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.*;

import org.openqa.selenium.By;

import java.net.MalformedURLException;
import java.time.Duration;

public class Hooks {
    public static final String APK_NAME = "radio-france.apk";
    public static final String WEB_URL = "https://www.radiofrance.fr/franceculture";
    protected WebDriverWait wait;
    private TestInfo testInfo;

    @Before
    public void beforeAll(Scenario scenario) throws MalformedURLException {
        // Test raporu için bilgileri ayarla
        testInfo = new TestInfo();
        testInfo.setScenarioName(scenario.getName());
        testInfo.setStepName("Test Başlangıcı");
        testInfo.setStatus("STARTED");

        // Platform bilgisini ayarla
        OS.OS = ConfigReader.getProperty("platformName");
        testInfo.setPlatform(OS.OS);

        TestReportManager.getInstance().addTestInfo(testInfo);

        // Platform spesifik başlatma işlemleri
        if (OS.isWeb()) {
            testInfo.setExpectedResult("Web tarayıcı başlatılmalı");
        } else if (OS.isAndroid()) {
            testInfo.setExpectedResult("Android uygulama başlatılmalı");
        }
    }

    @Given("Uygulamayı başlatıyorum")
    public void lanceApp() throws MalformedURLException {
        TestInfo appLaunchInfo = new TestInfo();
        appLaunchInfo.setStepName("Uygulama Başlatma");

        try {
            if (OS.isAndroid()) {
                System.out.println("Launching Android app: " + APK_NAME);
                Driver.Android = Driver.getAndroidDriver(Driver.getAndroidApps());
                appLaunchInfo.setStatus("PASSED");
                appLaunchInfo.setActualResult("Android uygulama başarıyla başlatıldı");
            } else if (OS.isWeb()) {
                System.out.println("Launching web app: " + WEB_URL);
                Driver.Web = Driver.getWebDriver(ConfigReader.getProperty("browser"));
                Driver.Web.get(WEB_URL);
                this.wait = new WebDriverWait(Driver.Web, Duration.ofSeconds(1));
                handlePopupsAndCookies();
                appLaunchInfo.setStatus("PASSED");
                appLaunchInfo.setActualResult("Web uygulama başarıyla başlatıldı");
                appLaunchInfo.setUrl(WEB_URL);
            }
        } catch (Exception e) {
            appLaunchInfo.setStatus("FAILED");
            appLaunchInfo.setErrorMessage("Uygulama başlatma hatası: " + e.getMessage());
            throw e;
        } finally {
            TestReportManager.getInstance().addTestInfo(appLaunchInfo);
        }
    }

    private void handlePopupsAndCookies() {
        TestInfo popupInfo = new TestInfo();
        popupInfo.setStepName("Popup ve Cookie Yönetimi");

        try {
            // Reklam popup'ı kapatma
            try {
                WebElement closeButton = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//span[text()='Tout refuser']")));
                closeButton.click();
                popupInfo.setActualResult("Reklam popup'ı kapatıldı");
            } catch (Exception e) {
                popupInfo.setActualResult("Reklam popup'ı bulunamadı veya zaten kapalı");
            }

            // Genel popup reddetme
            try {
                WebElement popupRefuse = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//span[text()='Tout refuser']")));
                popupRefuse.click();
                popupInfo.setActualResult(popupInfo.getActualResult() + "\nPopup reddedildi");
            } catch (Exception e) {
                popupInfo.setActualResult(popupInfo.getActualResult() + "\nPopup bulunamadı veya zaten reddedilmiş");
            }

            // Cookie kabul etme
            try {
                WebElement cookiesAccept = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//span[text()='Tout accepter']")));
                cookiesAccept.click();
                popupInfo.setActualResult(popupInfo.getActualResult() + "\nCookie'ler kabul edildi");
            } catch (Exception e) {
                popupInfo.setActualResult(popupInfo.getActualResult() + "\nCookie butonu bulunamadı veya zaten kabul edilmiş");
            }

            popupInfo.setStatus("PASSED");
        } catch (Exception e) {
            popupInfo.setStatus("WARNING");
            popupInfo.setErrorMessage("Popup işlemlerinde uyarı: " + e.getMessage());
        } finally {
            TestReportManager.getInstance().addTestInfo(popupInfo);
        }
    }

    @After
    public void tearDown(Scenario scenario) {
        testInfo = new TestInfo();
        testInfo.setScenarioName(scenario.getName());
        testInfo.setStepName("Test Sonlandırma");

        try {
            if (scenario.isFailed()) {
                testInfo.setStatus("FAILED");

                // Screenshot al
                WebDriver driver = Driver.getCurrentDriver();
                if (driver != null && driver instanceof TakesScreenshot) {
                    byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                    scenario.attach(screenshot, "image/png", "screenshot-" + scenario.getName());
                    testInfo.setActualResult("Test başarısız oldu - Screenshot eklendi");
                }
            } else {
                testInfo.setStatus("PASSED");
                testInfo.setActualResult("Test başarıyla tamamlandı");
            }

            // Son URL'yi kaydet
            WebDriver driver = Driver.getCurrentDriver();
            if (driver != null) {
                testInfo.setUrl(driver.getCurrentUrl());
            }

        } catch (Exception e) {
            testInfo.setStatus("ERROR");
            testInfo.setErrorMessage("Test sonlandırma hatası: " + e.getMessage());
        } finally {
            TestReportManager.getInstance().addTestInfo(testInfo);
            TestReportManager.getInstance().generateReport("RadioFrance");
            quitDriver();
        }
    }

    public static String getAppPackage() {
        return "com.radiofrance.radio.radiofrance.android";
    }

    private void killApplication(AndroidDriver driver) {
        if (driver != null) {
            driver.terminateApp(getAppPackage());
        }
    }

    private void quitDriver() {
        WebDriver driver = Driver.getCurrentDriver();
        if (driver != null) {
            try {
                Thread.sleep(5000); // 5 saniye bekle

                if (OS.OS.equals("Android")) {
                    killApplication((AndroidDriver) driver);
                }
                driver.quit();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}