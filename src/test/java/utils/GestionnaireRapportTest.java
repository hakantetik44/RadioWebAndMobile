package utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class GestionnaireRapportTest {
    private static GestionnaireRapportTest instance;
    private List<InfosTest> listeInfosTest;
    private static final String REPERTOIRE_RAPPORT = "target/rapports-tests";

    private GestionnaireRapportTest() {
        listeInfosTest = new ArrayList<>();
        creerRepertoireRapport();
    }

    private void creerRepertoireRapport() {
        try {
            Path cheminRapport = Paths.get(REPERTOIRE_RAPPORT);
            if (!Files.exists(cheminRapport)) {
                Files.createDirectories(cheminRapport);
            }
        } catch (IOException e) {
            System.err.println("Le répertoire de rapport n'a pas pu être créé : " + e.getMessage());
        }
    }

    public static GestionnaireRapportTest getInstance() {
        if (instance == null) {
            instance = new GestionnaireRapportTest();
        }
        return instance;
    }

    public void ajouterInfosTest(InfosTest infosTest) {
        if (infosTest != null) {
            listeInfosTest.add(infosTest);
        }
    }

    public void genererRapport(String nomTest) {
        String horodatage = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
                .format(java.time.LocalDateTime.now());
        String nomFichier = String.format("%s/%s_%s.xlsx", REPERTOIRE_RAPPORT, nomTest, horodatage);

        try (Workbook classeur = new XSSFWorkbook()) {
            Sheet feuille = classeur.createSheet("Résultats des Tests");

            // Style de l'en-tête
            CellStyle styleEnTete = classeur.createCellStyle();
            Font policeEnTete = classeur.createFont();
            policeEnTete.setBold(true);
            styleEnTete.setFont(policeEnTete);
            styleEnTete.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            styleEnTete.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Ligne de l'en-tête
            Row ligneEnTete = feuille.createRow(0);
            String[] colonnes = {
                    "Scénario", "Étape", "Statut", "Plateforme",
                    "Résultat Attendu", "Résultat Réel",
                    "Message d'Erreur", "URL", "Heure"
            };

            for (int i = 0; i < colonnes.length; i++) {
                Cell cellule = ligneEnTete.createCell(i);
                cellule.setCellValue(colonnes[i]);
                cellule.setCellStyle(styleEnTete);
                feuille.setColumnWidth(i, 6000);
            }

            // Remplir les données de test
            int numeroLigne = 1;
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (InfosTest infos : listeInfosTest) {
                Row ligne = feuille.createRow(numeroLigne++);

                ligne.createCell(0).setCellValue(infos.getNomScenario());
                ligne.createCell(1).setCellValue(infos.getNomEtape());
                ligne.createCell(2).setCellValue(infos.getStatut());
                ligne.createCell(3).setCellValue(infos.getPlateforme());
                ligne.createCell(4).setCellValue(infos.getResultatAttendu());
                ligne.createCell(5).setCellValue(infos.getResultatReel());
                ligne.createCell(6).setCellValue(infos.getMessageErreur());
                ligne.createCell(7).setCellValue(infos.getUrl());
                ligne.createCell(8).setCellValue(
                        infos.getHeureExecution().format(dtf)
                );

                // Style rouge pour les tests échoués
                if ("FAILED".equals(infos.getStatut())) {
                    CellStyle styleEchec = classeur.createCellStyle();
                    styleEchec.setFillForegroundColor(IndexedColors.ROSE.getIndex());
                    styleEchec.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    ligne.getCell(2).setCellStyle(styleEchec);
                }
            }

            // Sauvegarder le rapport
            try (FileOutputStream sortie = new FileOutputStream(nomFichier)) {
                classeur.write(sortie);
                System.out.println("Rapport de test créé : " + nomFichier);
            }

        } catch (IOException e) {
            System.err.println("Erreur de génération du rapport : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void effacerInfosTest() {
        listeInfosTest.clear();
    }

    public List<InfosTest> getListeInfosTest() {
        return new ArrayList<>(listeInfosTest);
    }
}
