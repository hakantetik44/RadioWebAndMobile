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

public class TestReportManager {
    private static TestReportManager instance; // Singleton instance
    private final List<TestInfo> listeInfosTest; // List to hold test info
    private static final String REPERTOIRE_RAPPORT = "target/rapports-tests"; // Directory for reports

    // Private constructor for singleton pattern
    private TestReportManager() {
        listeInfosTest = new ArrayList<>();
        creerRepertoireRapport(); // Create report directory
    }

    // Method to create report directory if it doesn't exist
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

    // Method to get the singleton instance
    public static TestReportManager getInstance() {
        if (instance == null) {
            instance = new TestReportManager();
        }
        return instance;
    }

    // Method to add test information
    public void ajouterInfosTest(TestInfo infosTest) {
        if (infosTest != null) {
            listeInfosTest.add(infosTest);
        }
    }

    // Method to generate the test report
    public void genererRapport(String nomTest) {
        String horodatage = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss").format(java.time.LocalDateTime.now());
        String nomFichier = String.format("%s/%s_%s.xlsx", REPERTOIRE_RAPPORT, nomTest, horodatage);

        try (Workbook classeur = new XSSFWorkbook()) {
            Sheet feuille = classeur.createSheet("Résultats des Tests");

            // Style for header
            CellStyle styleEnTete = classeur.createCellStyle();
            Font policeEnTete = classeur.createFont();
            policeEnTete.setBold(true);
            styleEnTete.setFont(policeEnTete);
            styleEnTete.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            styleEnTete.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Header row
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
                feuille.setColumnWidth(i, 6000); // Set column width
            }

            // Fill test data
            int numeroLigne = 1;
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (TestInfo infos : listeInfosTest) {
                Row ligne = feuille.createRow(numeroLigne++);

                ligne.createCell(0).setCellValue(infos.getNomScenario());
                ligne.createCell(1).setCellValue(infos.getNomEtape());
                ligne.createCell(2).setCellValue(infos.getStatut());
                ligne.createCell(3).setCellValue(infos.getPlateforme());
                ligne.createCell(4).setCellValue(infos.getResultatAttendu());
                ligne.createCell(5).setCellValue(infos.getResultatReel());
                ligne.createCell(6).setCellValue(infos.getMessageErreur());
                ligne.createCell(7).setCellValue(infos.getUrl());
                ligne.createCell(8).setCellValue(infos.getHeureExecution().format(dtf));

                // Style red for failed tests
                if ("FAILED".equalsIgnoreCase(infos.getStatut())) {
                    CellStyle styleEchec = classeur.createCellStyle();
                    styleEchec.setFillForegroundColor(IndexedColors.ROSE.getIndex());
                    styleEchec.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    ligne.getCell(2).setCellStyle(styleEchec);
                }
            }

            // Save the report
            try (FileOutputStream sortie = new FileOutputStream(nomFichier)) {
                classeur.write(sortie);
                System.out.println("Rapport de test créé : " + nomFichier);
            }

        } catch (IOException e) {
            System.err.println("Erreur de génération du rapport : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to clear test information
    public void effacerInfosTest() {
        listeInfosTest.clear();
    }

    // Getter for the list of test information
    public List<TestInfo> getListeInfosTest() {
        return new ArrayList<>(listeInfosTest);
    }
}
