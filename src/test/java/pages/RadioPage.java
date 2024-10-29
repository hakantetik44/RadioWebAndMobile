package pages;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.openqa.selenium.support.PageFactory;
import utils.OS;

import static utils.Driver.getCurrentDriver;

public class RadioPage extends BasePage {

    public RadioPage() {
        super(getCurrentDriver());
        PageFactory.initElements(getCurrentDriver(), this);
    }

    public void aramaYap(String searchTerm) {
        By ara = OS.isAndroid() ?
                AppiumBy.androidUIAutomator("new UiSelector().text(\"Recherche\")") :
                By.xpath("//input[@aria-label='Rechercher un podcast, un épisode, une personnalité...']");
        sendKeys(ara, searchTerm);
    }

    public void clickBtnRechercher() {
        By btnRecherche = OS.isAndroid() ?
                AppiumBy.accessibilityId("Recherche") :
                By.xpath("//span[normalize-space()='Rechercher']");
        getCurrentDriver().findElement(btnRecherche).click();
    }

    public void clikBtnSeConnecter() {
        By btnSeConnecter = OS.isAndroid() ?
                AppiumBy.accessibilityId("Se Connecter") :        //duzeltilecek
                By.xpath("//button[@data-testid='header-login-button']");
        getCurrentDriver().findElement(btnSeConnecter).click();
    }
    public void clikBtnInscription() {
        By btnInscription = OS.isAndroid() ?
                AppiumBy.accessibilityId("Inscription") :        //duzeltilecek
                By.xpath("/html/body/div[2]/footer/div[2]/div[2]/div/div/div/div[2]/div/div/nav/ul/li[2]");
        getCurrentDriver().findElement(btnInscription).click();
    }


}
