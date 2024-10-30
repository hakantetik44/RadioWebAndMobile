package utils;

import java.util.ArrayList;
import java.util.List;

public class TestManager {
    private static TestManager instance; // Unique instance
    private String nomScenario;
    private String nomEtape;
    private String statut;
    private String plateforme; // Platform name
    private String resultatAttendu;
    private String resultatReel;
    private String url;
    private String messageErreur;

    private List<TestManager> rapportsTests; // List of test reports

    public TestManager() {
        rapportsTests = new ArrayList<>();
    }

    public static TestManager getInstance() {
        if (instance == null) {
            instance = new TestManager();
        }
        return instance;
    }

    public String getNomScenario() {
        return nomScenario;
    }

    public void setNomScenario(String nomScenario) {
        this.nomScenario = nomScenario;
    }

    public String getNomEtape() {
        return nomEtape;
    }

    public void setNomEtape(String nomEtape) {
        this.nomEtape = nomEtape;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getPlateforme() {
        return plateforme;
    }

    public void setPlateforme(String plateforme) {
        this.plateforme = plateforme; // Set the platform name
    }

    public String getResultatAttendu() {
        return resultatAttendu;
    }

    public void setResultatAttendu(String resultatAttendu) {
        this.resultatAttendu = resultatAttendu; // Set expected result
    }

    public String getResultatReel() {
        return resultatReel;
    }

    public void setResultatReel(String resultatReel) {
        this.resultatReel = resultatReel; // Set actual result
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url; // Set URL
    }

    public String getMessageErreur() {
        return messageErreur;
    }

    public void setMessageErreur(String messageErreur) {
        this.messageErreur = messageErreur; // Set error message
    }

    public void ajouterInfosTest(TestManager testInfo) {
        rapportsTests.add(testInfo); // Add test info to the list
    }

    public void genererRapport(String nomRapport) {
        System.out.println("Génération du rapport: " + nomRapport);
        for (TestManager info : rapportsTests) {
            System.out.println("Nom du Scénario: " + info.getNomScenario());
            System.out.println("Nom de l'Étape: " + info.getNomEtape());
            System.out.println("Statut: " + info.getStatut());
            System.out.println("Plateforme: " + info.getPlateforme()); // Print platform
            System.out.println("Résultat Attendu: " + info.getResultatAttendu()); // Print expected result
            System.out.println("Résultat Réel: " + info.getResultatReel()); // Print actual result
            System.out.println("URL: " + info.getUrl()); // Print URL
            System.out.println("Message d'Erreur: " + info.getMessageErreur()); // Print error message
            System.out.println("-----------------------------------");
        }
    }
}