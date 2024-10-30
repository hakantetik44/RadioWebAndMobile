package stepdefinitions;

import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import pages.RadioPage;
import utils.Driver;
import utils.TestManager;

import static org.junit.Assert.assertTrue;

public class RadioStep {
    private RadioPage pageRadio = new RadioPage();
    private TestManager testManager;
    private static String currentScenarioName;
    private static final String PLATFORM = System.getProperty("platformName", "Web");

    public RadioStep() {
        testManager = new TestManager(); // Her step için yeni instance oluştur
    }

    @Before
    public void setUp(Scenario scenario) {
        currentScenarioName = scenario.getName();
        testManager.setNomScenario(currentScenarioName);
        testManager.setPlateforme(PLATFORM);
    }

    @Then("Je vérifie que je suis sur la page d'accueil")
    public void verifierPageAccueil() {
        try {
            testManager.setNomScenario(currentScenarioName);
            testManager.setPlateforme(PLATFORM);
            testManager.setNomEtape("Vérification de la page d'accueil");
            testManager.setResultatAttendu("La page d'accueil de Radio France doit être affichée");

            String urlAttendue = "https://www.radiofrance.fr";
            String urlReelle = Driver.getCurrentDriver().getCurrentUrl();
            testManager.setUrl(urlReelle);

            assertTrue(urlReelle.startsWith(urlAttendue));

            testManager.setStatut("REUSSI");
            testManager.setResultatReel("Page d'accueil affichée avec succès");

            // Tek bir test bilgisi ekle
            TestManager.getInstance().ajouterInfosTest(testManager);

        } catch (Exception e) {
            testManager.setStatut("ECHEC");
            testManager.setResultatReel("Erreur d'accès à la page d'accueil");
            testManager.setMessageErreur(e.getMessage());
            TestManager.getInstance().ajouterInfosTest(testManager);
            throw e;
        }
    }

    @When("Je clique sur le bouton {string}")
    public void cliquerSurLeBouton(String nomBouton) {
        try {
            testManager.setNomScenario(currentScenarioName);
            testManager.setPlateforme(PLATFORM);
            testManager.setNomEtape("Clic sur le bouton " + nomBouton);
            testManager.setResultatAttendu("Le bouton " + nomBouton + " doit être cliqué");

            pageRadio.cliquerBtnRechercher();

            testManager.setStatut("REUSSI");
            testManager.setResultatReel("Bouton " + nomBouton + " cliqué avec succès");

            TestManager.getInstance().ajouterInfosTest(testManager);

        } catch (Exception e) {
            testManager.setStatut("ECHEC");
            testManager.setResultatReel("Erreur lors du clic sur le bouton");
            testManager.setMessageErreur(e.getMessage());
            TestManager.getInstance().ajouterInfosTest(testManager);
            throw e;
        }
    }

    @When("Je saisis {string} dans le champ de recherche")
    public void effectuerRecherche(String termeRecherche) {
        try {
            testManager.setNomScenario(currentScenarioName);
            testManager.setPlateforme(PLATFORM);
            testManager.setNomEtape("Saisie dans le champ de recherche");
            testManager.setResultatAttendu("Le terme '" + termeRecherche + "' doit être saisi");

            pageRadio.effectuerRecherche(termeRecherche);

            testManager.setStatut("REUSSI");
            testManager.setResultatReel("Recherche '" + termeRecherche + "' effectuée");
            testManager.setUrl(Driver.getCurrentDriver().getCurrentUrl());

            TestManager.getInstance().ajouterInfosTest(testManager);

        } catch (Exception e) {
            testManager.setStatut("ECHEC");
            testManager.setResultatReel("Erreur lors de la saisie");
            testManager.setMessageErreur(e.getMessage());
            TestManager.getInstance().ajouterInfosTest(testManager);
            throw e;
        }
    }

    @Then("Les résultats pour {string} doivent être affichés")
    public void verifierResultatsRecherche(String termeRecherche) throws InterruptedException {
        try {
            testManager.setNomScenario(currentScenarioName);
            testManager.setPlateforme(PLATFORM);
            testManager.setNomEtape("Vérification des résultats de recherche");
            testManager.setResultatAttendu("Les résultats pour '" + termeRecherche + "' doivent être affichés");

            Thread.sleep(3000);
            String urlAttendue = "https://www.radiofrance.fr/recherche";
            String urlReelle = Driver.getCurrentDriver().getCurrentUrl();

            testManager.setUrl(urlReelle);
            assertTrue(urlReelle.startsWith(urlAttendue));

            testManager.setStatut("REUSSI");
            testManager.setResultatReel("Résultats affichés pour '" + termeRecherche + "'");

            TestManager.getInstance().ajouterInfosTest(testManager);

        } catch (Exception e) {
            testManager.setStatut("ECHEC");
            testManager.setResultatReel("Erreur lors de la vérification");
            testManager.setMessageErreur(e.getMessage());
            TestManager.getInstance().ajouterInfosTest(testManager);
            throw e;
        }
    }
}