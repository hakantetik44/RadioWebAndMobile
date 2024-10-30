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
    public static final String NOM_APK = "radio-france.apk";
    public static final String URL_WEB = "https://www.radiofrance.fr/franceculture";
    protected WebDriverWait attente;
    private TestInfo infosTest;

    @Before
    public void avantTout(Scenario scenario) throws MalformedURLException {
        // Initialiser les informations pour le rapport de test
        infosTest = new TestInfo();
        infosTest.setNomScenario(scenario.getName());
        infosTest.setNomEtape("Début du Test");
        infosTest.setStatut("DÉMARRÉ");

        // Définir la plateforme
        OS.OS = ConfigReader.getProperty("platformName");
        infosTest.setPlateforme(OS.OS);

        TestReportManager.getInstance().ajouterInfosTest(infosTest);

        // Initialisations spécifiques à la plateforme
        if (OS.isWeb()) {
            infosTest.setResultatAttendu("Le navigateur web doit être lancé");
        } else if (OS.isAndroid()) {
            infosTest.setResultatAttendu("L'application Android doit être lancée");
        }
    }

    @Given("Je lance l'application")
    public void lanceApp() throws MalformedURLException {
        TestInfo infosLancementApp = new TestInfo();
        infosLancementApp.setNomEtape("Lancement de l'Application");

        try {
            if (OS.isAndroid()) {
                System.out.println("Lancement de l'application Android : " + NOM_APK);
                Driver.Android = Driver.getAndroidDriver(Driver.getAndroidApps());
                infosLancementApp.setStatut("RÉUSSI");
                infosLancementApp.setResultatReel("L'application Android a été lancée avec succès");
            } else if (OS.isWeb()) {
                System.out.println("Lancement de l'application web : " + URL_WEB);
                Driver.Web = Driver.getWebDriver(ConfigReader.getProperty("browser"));
                Driver.Web.get(URL_WEB);
                this.attente = new WebDriverWait(Driver.Web, Duration.ofSeconds(1));
                gererPopupsEtCookies();
                infosLancementApp.setStatut("RÉUSSI");
                infosLancementApp.setResultatReel("L'application web a été lancée avec succès");
                infosLancementApp.setUrl(URL_WEB);
            }
        } catch (Exception e) {
            infosLancementApp.setStatut("ÉCHOUÉ");
            infosLancementApp.setMessageErreur("Erreur de lancement de l'application : " + e.getMessage());
            throw e;
        } finally {
            TestReportManager.getInstance().ajouterInfosTest(infosLancementApp);
        }
    }

    private void gererPopupsEtCookies() {
        TestInfo infosPopup = new TestInfo();
        infosPopup.setNomEtape("Gestion des Popups et Cookies");

        try {
            // Fermer le popup de publicité
            try {
                WebElement boutonFermer = attente.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//span[text()='Tout refuser']")));
                boutonFermer.click();
                infosPopup.setResultatReel("Popup de publicité fermé");
            } catch (Exception e) {
                infosPopup.setResultatReel("Popup de publicité non trouvé ou déjà fermé");
            }

            // Refuser les autres popups
            try {
                WebElement popupRefuser = attente.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//span[text()='Tout refuser']")));
                popupRefuser.click();
                infosPopup.setResultatReel(infosPopup.getResultatReel() + "\nPopup refusé");
            } catch (Exception e) {
                infosPopup.setResultatReel(infosPopup.getResultatReel() + "\nPopup non trouvé ou déjà refusé");
            }

            // Accepter les cookies
            try {
                WebElement accepterCookies = attente.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//span[text()='Tout accepter']")));
                accepterCookies.click();
                infosPopup.setResultatReel(infosPopup.getResultatReel() + "\nCookies acceptés");
            } catch (Exception e) {
                infosPopup.setResultatReel(infosPopup.getResultatReel() + "\nBouton de cookies non trouvé ou déjà accepté");
            }

            infosPopup.setStatut("RÉUSSI");
        } catch (Exception e) {
            infosPopup.setStatut("AVERTISSEMENT");
            infosPopup.setMessageErreur("Avertissement lors du traitement des popups : " + e.getMessage());
        } finally {
            TestReportManager.getInstance().ajouterInfosTest(infosPopup);
        }
    }

    @After
    public void terminer(Scenario scenario) {
        infosTest = new TestInfo();
        infosTest.setNomScenario(scenario.getName());
        infosTest.setNomEtape("Fin du Test");

        try {
            if (scenario.isFailed()) {
                infosTest.setStatut("ÉCHOUÉ");

                // Prendre une capture d'écran
                WebDriver driver = Driver.getCurrentDriver();
                if (driver != null && driver instanceof TakesScreenshot) {
                    byte[] captureEcran = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                    scenario.attach(captureEcran, "image/png", "capture-" + scenario.getName());
                    infosTest.setResultatReel("Test échoué - Capture d'écran ajoutée");
                }
            } else {
                infosTest.setStatut("RÉUSSI");
                infosTest.setResultatReel("Test terminé avec succès");
            }

            // Enregistrer l'URL finale
            WebDriver driver = Driver.getCurrentDriver();
            if (driver != null) {
                infosTest.setUrl(driver.getCurrentUrl());
            }

        } catch (Exception e) {
            infosTest.setStatut("ERREUR");
            infosTest.setMessageErreur("Erreur lors de la fin du test : " + e.getMessage());
        } finally {
            TestReportManager.getInstance().ajouterInfosTest(infosTest);
            TestReportManager.getInstance().genererRapport("RadioFrance");
            quitterDriver();
        }
    }

    public static String getAppPackage() {
        return "com.radiofrance.radio.radiofrance.android";
    }

    private void terminerApplication(AndroidDriver driver) {
        if (driver != null) {
            driver.terminateApp(getAppPackage());
        }
    }

    private void quitterDriver() {
        WebDriver driver = Driver.getCurrentDriver();
        if (driver != null) {
            try {
                Thread.sleep(5000); // Attendre 5 secondes

                if (OS.OS.equals("Android")) {
                    terminerApplication((AndroidDriver) driver);
                }
                driver.quit();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
