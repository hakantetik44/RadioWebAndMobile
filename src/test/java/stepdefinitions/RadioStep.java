package stepdefinitions;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import pages.RadioPage;
import utils.Driver;
import utils.TestInfo;
import utils.TestReportManager;

import static org.junit.Assert.assertEquals;

public class RadioStep{
    private RadioPage pageRadio = new RadioPage();
    private TestInfo infosTest;

    public RadioStep() {
        infosTest = new TestInfo();
    }

    @Then("Je vérifie que je suis sur la page d'accueil")
    public void verifierPageAccueil() {
        infosTest.setNomEtape("Vérification de la page d'accueil");
        try {
            // Code existant...
            infosTest.setStatut("RÉUSSI");
        } catch (Exception e) {
            infosTest.setStatut("ÉCHOUÉ");
            infosTest.setMessageErreur(e.getMessage());
            throw e;
        } finally {
            TestReportManager.getInstance().ajouterInfosTest(infosTest);
        }
    }

    @When("Je clique sur le bouton {string}")
    public void cliquerSurLeBouton(String nomBoutonRecherche) {
        infosTest = new TestInfo();
        infosTest.setNomEtape("Clic sur le bouton de recherche : " + nomBoutonRecherche);

        try {
            pageRadio.cliquerBtnRechercher();
            infosTest.setStatut("RÉUSSI");
        } catch (Exception e) {
            infosTest.setStatut("ÉCHOUÉ");
            infosTest.setMessageErreur(e.getMessage());
            throw e;
        } finally {
            TestReportManager.getInstance().ajouterInfosTest(infosTest);
        }
    }

    @When("Je saisis {string} dans le champ de recherche")
    public void effectuerRecherche(String histoire) {
        infosTest = new TestInfo();
        infosTest.setNomEtape("Effectuer une recherche");
        infosTest.setResultatAttendu("Le texte '" + histoire + "' doit être saisi dans le champ de recherche");

        try {
            pageRadio.effectuerRecherche(histoire);
            infosTest.setStatut("RÉUSSI");
            infosTest.setResultatReel("Recherche effectuée : " + histoire);
        } catch (Exception e) {
            infosTest.setStatut("ÉCHOUÉ");
            infosTest.setMessageErreur(e.getMessage());
            throw e;
        } finally {
            TestReportManager.getInstance().ajouterInfosTest(infosTest);
        }
    }


    @Then("Les résultats pour {string} doivent être affichés")
    public void lesResultatsPourDoiventEtreAffiches(String histoire) throws InterruptedException {
        infosTest = new TestInfo();
        infosTest.setNomEtape("Vérification des résultats de recherche");

        String urlAttendue = "https://www.radiofrance.fr/recherche";
        infosTest.setResultatAttendu("URL attendue : " + urlAttendue);

        try {
            String urlReelle = Driver.getCurrentDriver().getCurrentUrl();
            infosTest.setResultatReel("URL actuelle : " + urlReelle);
            infosTest.setUrl(urlReelle);

            Thread.sleep(3000);
            assertEquals(urlAttendue, urlReelle);

            infosTest.setStatut("RÉUSSI");
        } catch (Exception e) {
            infosTest.setStatut("ÉCHOUÉ");
            infosTest.setMessageErreur(e.getMessage());
            throw e;
        } finally {
            TestReportManager.getInstance().ajouterInfosTest(infosTest);
        }
    }
}
