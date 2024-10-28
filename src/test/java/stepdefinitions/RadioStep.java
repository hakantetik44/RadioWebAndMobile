package stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import pages.RadioPage;
import utils.Driver;

import static org.junit.Assert.assertEquals;

public class RadioStep {

    RadioPage radioPage = new RadioPage();
    public RadioStep(){

    }


    @Then("Ana sayfada olduğumu doğruluyorum")
    public void anaSayfayiDogrula() {

    }
    @When("{string} düğmesine tıklarsam")
    public void düğmesineTikla(String aramaButonu) {
        radioPage.clickBtnRechercher();
    }

    @When("Arama alanına {string} yazarsam")
    public void aramaYap(String histoire) {
        radioPage.aramaYap(histoire);

    }
    @Then("Histoire için sonuçlar gorunmeli")
    public void sonuclariDogrula() throws InterruptedException {

        String expectedUrl = "https://www.radiofrance.fr/recherche?term=Histoire";
        System.out.println("expectedUrl = " + expectedUrl);
        String actualUrl = Driver.getCurrentDriver().getCurrentUrl();
        System.out.println("actualUrl = " + actualUrl);
        Thread.sleep(3000);
        assertEquals(expectedUrl, actualUrl);
    }
    @Given("Click le button Se Connecter")
    public void click_le_button_se_connecter() {
       radioPage.clikBtnSeConnecter();
    }
    @Then("Click sur Inscription")
    public void click_sur_inscription() {
        radioPage.clikBtnInscription();
    }










}
