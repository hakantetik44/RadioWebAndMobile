
package utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class TestManager {
    // Attributs privés pour le singleton et les données de test
    private static TestManager instance;
    private String nomScenario;
    private String nomEtape;
    private String statut;
    private String plateforme;
    private String resultatAttendu;
    private String resultatReel;
    private String url;
    private String messageErreur;
    private LocalDateTime dateExecution;

    // Collections pour stocker les rapports et les analyses
    private List<TestManager> rapportsTests;
    private Map<String, String> analysisResults;

    // Constantes pour les chemins et la plateforme
    private static final String EXCEL_REPORTS_DIR = "target/rapports-tests";
    private static final String PLATFORM = System.getProperty("platformName", "Web");

    // Constructeur privé pour le singleton
    public TestManager() {
        rapportsTests = new ArrayList<>();
        analysisResults = new HashMap<>();
        dateExecution = LocalDateTime.now();
        createReportsDirectory();
        this.plateforme = PLATFORM;
    }

    // Méthode pour créer le répertoire des rapports
    private void createReportsDirectory() {
        File directory = new File(EXCEL_REPORTS_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    // Méthode singleton pour obtenir l'instance
    public static TestManager getInstance() {
        if (instance == null) {
            instance = new TestManager();
        }
        return instance;
    }

    // Méthodes Getter et Setter
    public String getNomScenario() { return nomScenario; }
    public void setNomScenario(String nomScenario) { this.nomScenario = nomScenario; }

    public String getNomEtape() { return nomEtape; }
    public void setNomEtape(String nomEtape) { this.nomEtape = nomEtape; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getPlateforme() { return plateforme; }
    public void setPlateforme(String plateforme) { this.plateforme = plateforme; }

    public String getResultatAttendu() { return resultatAttendu; }
    public void setResultatAttendu(String resultatAttendu) { this.resultatAttendu = resultatAttendu; }

    public String getResultatReel() { return resultatReel; }
    public void setResultatReel(String resultatReel) { this.resultatReel = resultatReel; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getMessageErreur() { return messageErreur; }
    public void setMessageErreur(String messageErreur) { this.messageErreur = messageErreur; }

    // Méthode pour ajouter des informations de test
    public void ajouterInfosTest(TestManager testInfo) {
        boolean isDuplicate = rapportsTests.stream()
                .anyMatch(existing -> isSameStep(existing, testInfo));

        if (!isDuplicate) {
            testInfo.dateExecution = LocalDateTime.now();
            testInfo.plateforme = PLATFORM;
            rapportsTests.add(testInfo);
        }
    }

    // Méthode pour vérifier les doublons
    private boolean isSameStep(TestManager existing, TestManager newInfo) {
        return Objects.equals(existing.getNomEtape(), newInfo.getNomEtape()) &&
                Objects.equals(existing.getNomScenario(), newInfo.getNomScenario()) &&
                Objects.equals(existing.getUrl(), newInfo.getUrl());
    }

    // Méthode d'analyse des résultats des tests
    public String analyzeTestResults() {
        int totalTests = rapportsTests.size();
        if (totalTests == 0) return "Aucun résultat de test disponible.";

        int passedTests = 0;
        int failedTests = 0;
        Map<String, Integer> stepFailures = new HashMap<>();
        Set<String> uniqueScenarios = new HashSet<>();

        for (TestManager test : rapportsTests) {
            uniqueScenarios.add(test.getNomScenario());

            if ("REUSSI".equalsIgnoreCase(test.getStatut())) {
                passedTests++;
            } else {
                failedTests++;
                stepFailures.merge(test.getNomEtape(), 1, Integer::sum);
            }
        }

        double successRate = (passedTests * 100.0) / totalTests;

        StringBuilder analysis = new StringBuilder();
        analysis.append(String.format("📊 Analyse des Tests:\n"));
        analysis.append(String.format("• Total des Étapes: %d\n", totalTests));
        analysis.append(String.format("• Réussis: %d (%.1f%%)\n", passedTests, successRate));
        analysis.append(String.format("• Échoués: %d (%.1f%%)\n", failedTests, 100 - successRate));
        analysis.append(String.format("• Nombre de Scénarios: %d\n", uniqueScenarios.size()));

        if (!stepFailures.isEmpty()) {
            analysis.append("\n🔍 Étapes les Plus Problématiques:\n");
            stepFailures.entrySet().stream()
                    .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                    .limit(3)
                    .forEach(e -> analysis.append(String.format("• %s: %d fois\n", e.getKey(), e.getValue())));
        }

        // Analyse de la durée des tests
        if (totalTests > 1) {
            long totalDuration = 0;
            TestManager firstTest = rapportsTests.get(0);
            TestManager lastTest = rapportsTests.get(rapportsTests.size() - 1);
            totalDuration = java.time.Duration.between(firstTest.dateExecution, lastTest.dateExecution).getSeconds();

            analysis.append(String.format("\n⏱️ Performance:\n"));
            analysis.append(String.format("• Durée Totale: %d secondes\n", totalDuration));
            analysis.append(String.format("• Durée Moyenne par Étape: %.1f secondes\n", totalDuration / (double) totalTests));
        }

        // Suggestions d'amélioration
        analysis.append("\n💡 Recommandations:\n");
        if (successRate < 100) {
            analysis.append("• Examiner les tests échoués\n");
            if (!stepFailures.isEmpty()) {
                analysis.append("• Optimiser les étapes fréquemment échouées\n");
            }
        }
        if (totalTests < 5) {
            analysis.append("• Augmenter la couverture des tests\n");
        }

        analysisResults.put("test_analysis", analysis.toString());
        return analysis.toString();
    }

    // Méthode de suggestion d'améliorations
    public String suggestTestImprovements() {
        StringBuilder suggestions = new StringBuilder();
        suggestions.append("🔄 Suggestions d'Amélioration:\n\n");

        Set<String> uniqueScenarios = rapportsTests.stream()
                .map(TestManager::getNomScenario)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (uniqueScenarios.size() < 3) {
            suggestions.append("1. Diversité des Tests:\n");
            suggestions.append("   • Ajouter plus de scénarios de test\n");
            suggestions.append("   • Tester différents parcours utilisateur\n\n");
        }

        // Analyse des erreurs
        Map<String, Long> errorPatterns = rapportsTests.stream()
                .filter(t -> t.getMessageErreur() != null && !t.getMessageErreur().isEmpty())
                .collect(Collectors.groupingBy(
                        TestManager::getMessageErreur,
                        Collectors.counting()
                ));

        if (!errorPatterns.isEmpty()) {
            suggestions.append("2. Analyse des Erreurs:\n");
            errorPatterns.forEach((error, count) -> {
                suggestions.append(String.format("   • L'erreur '%s' s'est produite %d fois\n",
                        error.substring(0, Math.min(50, error.length())), count));
            });
            suggestions.append("\n");
        }

        // Analyse par plateforme
        Map<String, Long> platformStats = rapportsTests.stream()
                .collect(Collectors.groupingBy(
                        TestManager::getPlateforme,
                        Collectors.counting()
                ));

        suggestions.append("3. Distribution par Plateforme:\n");
        platformStats.forEach((platform, count) -> {
            suggestions.append(String.format("   • %s: %d tests\n", platform, count));
        });

        analysisResults.put("improvement_suggestions", suggestions.toString());
        return suggestions.toString();
    }

    // Méthode de génération du rapport Excel
    public void genererRapport(String nomRapport) {
        analyzeTestResults();
        suggestTestImprovements();

        String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String fileName = String.format("%s/%s_%s.xlsx", EXCEL_REPORTS_DIR, nomRapport, timeStamp);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet testSheet = workbook.createSheet("Résultats des Tests");
            createTestResultsSheet(testSheet, workbook);

            Sheet analysisSheet = workbook.createSheet("Analyse");
            createAnalysisSheet(analysisSheet);

            saveWorkbook(workbook, fileName);

        } catch (IOException e) {
            System.err.println("Erreur lors de la génération du rapport Excel: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Méthode de création de la feuille d'analyse
    private void createAnalysisSheet(Sheet sheet) {
        int rowNum = 0;

        CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        Font headerFont = sheet.getWorkbook().createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        // Section Analyse
        Row analysisHeaderRow = sheet.createRow(rowNum++);
        Cell headerCell = analysisHeaderRow.createCell(0);
        headerCell.setCellValue("Analyse des Tests");
        headerCell.setCellStyle(headerStyle);

        String analysis = analysisResults.get("test_analysis");
        String[] analysisLines = analysis.split("\n");
        for (String line : analysisLines) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(line);
        }

        rowNum++; // Ligne vide

        // Section Suggestions
        Row suggestionsHeaderRow = sheet.createRow(rowNum++);
        Cell suggestionsHeaderCell = suggestionsHeaderRow.createCell(0);
        suggestionsHeaderCell.setCellValue("Suggestions d'Amélioration");
        suggestionsHeaderCell.setCellStyle(headerStyle);

        String suggestions = analysisResults.get("improvement_suggestions");
        String[] suggestionLines = suggestions.split("\n");
        for (String line : suggestionLines) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(line);
        }

        sheet.setColumnWidth(0, 15000);
    }

    // Méthode de création de la feuille des résultats de tests
    private void createTestResultsSheet(Sheet sheet, Workbook workbook) {
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle successStyle = createSuccessStyle(workbook);
        CellStyle failureStyle = createFailureStyle(workbook);

        // En-têtes
        Row headerRow = sheet.createRow(0);
        String[] columns = {
                "Scénario", "Étape", "Statut", "Plateforme",
                "Résultat Attendu", "Résultat Réel", "URL",
                "Message d'Erreur", "Date d'Exécution"
        };

        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 6000);
        }

        // Données
        int rowNum = 1;
        for (TestManager info : rapportsTests) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(info.getNomScenario() != null ? info.getNomScenario() : "");
            row.createCell(1).setCellValue(info.getNomEtape() != null ? info.getNomEtape() : "");

            Cell statutCell = row.createCell(2);
            statutCell.setCellValue(info.getStatut() != null ? info.getStatut() : "");

            if ("REUSSI".equalsIgnoreCase(info.getStatut())) {
                statutCell.setCellStyle(successStyle);
            } else if ("ECHEC".equalsIgnoreCase(info.getStatut())) {
                statutCell.setCellStyle(failureStyle);
            }

            row.createCell(3).setCellValue(info.getPlateforme() != null ? info.getPlateforme() : PLATFORM);
            row.createCell(4).setCellValue(info.getResultatAttendu() != null ? info.getResultatAttendu() : "");
            row.createCell(5).setCellValue(info.getResultatReel() != null ? info.getResultatReel() : "");
            row.createCell(6).setCellValue(info.getUrl() != null ? info.getUrl() : "");
            row.createCell(7).setCellValue(info.getMessageErreur() != null ? info.getMessageErreur() : "");
            row.createCell(8).setCellValue(
                    info.dateExecution.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            );
        }
    }

    // Méthodes de style pour Excel
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createSuccessStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createFailureStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.ROSE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private void saveWorkbook(Workbook workbook, String fileName) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(fileName)) {
            workbook.write(outputStream);
            System.out.println("Rapport Excel généré avec succès: " + fileName);
        }
    }
}
