package stepdefinitions;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import pages.RadioPage;
import utils.Driver;
import utils.TestManager;

import static org.junit.Assert.assertEquals;

public class RadioStep {
    private RadioPage pageRadio = new RadioPage();
    private TestManager testManager;

    public RadioStep() {
        testManager = new TestManager();
    }

    @Then("Je vérifie que je suis sur la page d'accueil")
    public void verifierPageAccueil() {
        testManager.setNomEtape("Vérification de la page d'accueil");
        try {
            String urlAttendue = "https://www.radiofrance.fr";
            String urlReelle = Driver.getCurrentDriver().getCurrentUrl();
            assertEquals(urlAttendue, urlReelle);
            testManager.setStatut("RÉUSSI");
            testManager.setResultatReel("L'utilisateur est sur la page d'accueil.");
        } catch (Exception e) {
            testManager.setStatut("ÉCHOUÉ");
            testManager.setMessageErreur(e.getMessage());
            throw e;
        } finally {
            TestManager.getInstance().ajouterInfosTest(testManager);
        }
    }

    @When("Je clique sur le bouton {string}")
    public void cliquerSurLeBouton(String nomBoutonRecherche) {
        testManager = new TestManager();
        testManager.setNomEtape("Clic sur le bouton de recherche : " + nomBoutonRecherche);

        try {
            pageRadio.cliquerBtnRechercher();
            testManager.setStatut("RÉUSSI");
            testManager.setResultatReel("Bouton '" + nomBoutonRecherche + "' cliqué avec succès.");
        } catch (Exception e) {
            testManager.setStatut("ÉCHOUÉ");
            testManager.setMessageErreur(e.getMessage());
            throw e;
        } finally {
            TestManager.getInstance().ajouterInfosTest(testManager);
        }
    }

    @When("Je saisis {string} dans le champ de recherche")
    public void effectuerRecherche(String histoire) {
        testManager = new TestManager();
        testManager.setNomEtape("Effectuer une recherche");
        testManager.setResultatAttendu("Le texte '" + histoire + "' doit être saisi dans le champ de recherche");

        try {
            pageRadio.effectuerRecherche(histoire);
            testManager.setStatut("RÉUSSI");
            testManager.setResultatReel("Recherche effectuée : " + histoire);
        } catch (Exception e) {
            testManager.setStatut("ÉCHOUÉ");
            testManager.setMessageErreur(e.getMessage());
            throw e;
        } finally {
            TestManager.getInstance().ajouterInfosTest(testManager);
        }
    }

    @Then("Les résultats pour {string} doivent être affichés")
    public void lesResultatsPourDoiventEtreAffiches(String histoire) throws InterruptedException {
        testManager = new TestManager();
        testManager.setNomEtape("Vérification des résultats de recherche");

        String urlAttendue = "https://www.radiofrance.fr/recherche";
        testManager.setResultatAttendu("URL attendue : " + urlAttendue);

        try {
            Thread.sleep(3000);
            String urlReelle = Driver.getCurrentDriver().getCurrentUrl();
            testManager.setResultatReel("URL actuelle : " + urlReelle);
            testManager.setUrl(urlReelle);
            assertEquals(urlAttendue, urlReelle);
            testManager.setStatut("RÉUSSI");
        } catch (Exception e) {
            testManager.setStatut("ÉCHOUÉ");
            testManager.setMessageErreur(e.getMessage());
            throw e;
        } finally {
            TestManager.getInstance().ajouterInfosTest(testManager);
        }
    }
}
