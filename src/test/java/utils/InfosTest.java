package utils;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InfosTest {
    private String nomScenario;
    private String nomEtape;
    private String statut;
    private String resultatAttendu;
    private String resultatReel;
    private String messageErreur;
    private LocalDateTime heureExecution;
    private String plateforme;
    private String url;
    private String journauxNavigateur;

    public InfosTest() {
        this.heureExecution = LocalDateTime.now();
    }

    // Setter pour la plateforme
    public void setPlateforme(String plateforme) {
        this.plateforme = plateforme;
    }

    // Méthodes getter
    public String getNomScenario() {
        return nomScenario;
    }

    public String getNomEtape() {
        return nomEtape;
    }

    public String getStatut() {
        return statut;
    }

    public String getResultatAttendu() {
        return resultatAttendu;
    }

    public String getResultatReel() {
        return resultatReel;
    }

    public String getMessageErreur() {
        return messageErreur;
    }

    public LocalDateTime getHeureExecution() {
        return heureExecution;
    }

    public String getPlateforme() {
        return plateforme;
    }

    public String getUrl() {
        return url;
    }

    public String getJournauxNavigateur() {
        return journauxNavigateur;
    }

    // Méthodes setter
    public void setNomScenario(String nomScenario) {
        this.nomScenario = nomScenario;
    }

    public void setNomEtape(String nomEtape) {
        this.nomEtape = nomEtape;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public void setResultatAttendu(String resultatAttendu) {
        this.resultatAttendu = resultatAttendu;
    }

    public void setResultatReel(String resultatReel) {
        this.resultatReel = resultatReel;
    }

    public void setMessageErreur(String messageErreur) {
        this.messageErreur = messageErreur;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setJournauxNavigateur(String journauxNavigateur) {
        this.journauxNavigateur = journauxNavigateur;
    }
}
