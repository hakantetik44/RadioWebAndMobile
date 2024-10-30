package utils;

import java.time.LocalDateTime;

public class TestInfo {
    private String nomScenario;
    private String nomEtape;
    private String statut;
    private String plateforme;
    private String resultatAttendu;
    private String resultatReel;
    private String messageErreur;
    private String url;
    private LocalDateTime heureExecution;

    public TestInfo() {
        this.heureExecution = LocalDateTime.now();
    }

    // Getters
    public String getNomScenario() { return nomScenario; }
    public String getNomEtape() { return nomEtape; }
    public String getStatut() { return statut; }
    public String getPlateforme() { return plateforme; }
    public String getResultatAttendu() { return resultatAttendu; }
    public String getResultatReel() { return resultatReel; }
    public String getMessageErreur() { return messageErreur; }
    public String getUrl() { return url; }
    public LocalDateTime getHeureExecution() { return heureExecution; }

    // Setters
    public void setNomScenario(String nomScenario) { this.nomScenario = nomScenario; }
    public void setNomEtape(String nomEtape) { this.nomEtape = nomEtape; }
    public void setStatut(String statut) { this.statut = statut; }
    public void setPlateforme(String plateforme) { this.plateforme = plateforme; }
    public void setResultatAttendu(String resultatAttendu) { this.resultatAttendu = resultatAttendu; }
    public void setResultatReel(String resultatReel) { this.resultatReel = resultatReel; }
    public void setMessageErreur(String messageErreur) { this.messageErreur = messageErreur; }
    public void setUrl(String url) { this.url = url; }
    public void setHeureExecution(LocalDateTime heureExecution) { this.heureExecution = heureExecution; }
}
