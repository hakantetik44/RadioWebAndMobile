package utils;

import java.util.ArrayList;
import java.util.List;

public class TestManager {
    private static TestManager instance; // Instance unique
    private String nomScenario;
    private String nomEtape;
    private String statut;
    private String plateforme;
    private String resultatAttendu;
    private String resultatReel;
    private String url;
    private String messageErreur;

    private List<TestManager> rapportsTests;

    // Constructeur privé pour empêcher l'instanciation directe
    public TestManager() {
        rapportsTests = new ArrayList<>();
    }

    // Méthode pour obtenir l'instance unique
    public static TestManager getInstance() {
        if (instance == null) {
            instance = new TestManager();
        }
        return instance;
    }

    // Getters et Setters
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
        this.plateforme = plateforme;
    }

    public String getResultatAttendu() {
        return resultatAttendu;
    }

    public void setResultatAttendu(String resultatAttendu) {
        this.resultatAttendu = resultatAttendu;
    }

    public String getResultatReel() {
        return resultatReel;
    }

    public void setResultatReel(String resultatReel) {
        this.resultatReel = resultatReel;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMessageErreur() {
        return messageErreur;
    }

    public void setMessageErreur(String messageErreur) {
        this.messageErreur = messageErreur;
    }

    public void ajouterInfosTest(TestManager testInfo) {
        rapportsTests.add(testInfo);
    }

    public void genererRapport(String nomRapport) {
        System.out.println("Génération du rapport: " + nomRapport);
        for (TestManager info : rapportsTests) {
            System.out.println("Nom du Scénario: " + info.getNomScenario());
            System.out.println("Nom de l'Étape: " + info.getNomEtape());
            System.out.println("Statut: " + info.getStatut());
            System.out.println("Plateforme: " + info.getPlateforme());
            System.out.println("Résultat Attendu: " + info.getResultatAttendu());
            System.out.println("Résultat Réel: " + info.getResultatReel());
            System.out.println("URL: " + info.getUrl());
            System.out.println("Message d'Erreur: " + info.getMessageErreur());
            System.out.println("-----------------------------------");
        }
    }
}
