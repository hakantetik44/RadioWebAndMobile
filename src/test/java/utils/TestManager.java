package utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TestManager {
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

    private List<TestManager> rapportsTests;
    private static final String EXCEL_REPORTS_DIR = "target/rapports-tests";
    private static final String PLATFORM = System.getProperty("platformName", "Web");

    public TestManager() {
        rapportsTests = new ArrayList<>();
        dateExecution = LocalDateTime.now();
        createReportsDirectory();
        this.plateforme = PLATFORM;
    }

    private void createReportsDirectory() {
        File directory = new File(EXCEL_REPORTS_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    public static TestManager getInstance() {
        if (instance == null) {
            instance = new TestManager();
        }
        return instance;
    }

    public static TestManager createTestInfo(TestManager source) {
        TestManager newInfo = new TestManager();
        newInfo.setNomScenario(source.getNomScenario());
        newInfo.setNomEtape(source.getNomEtape());
        newInfo.setStatut(source.getStatut());
        newInfo.setPlateforme(PLATFORM);
        newInfo.setResultatAttendu(source.getResultatAttendu());
        newInfo.setResultatReel(source.getResultatReel());
        newInfo.setUrl(source.getUrl());
        newInfo.setMessageErreur(source.getMessageErreur());
        return newInfo;
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
        boolean isDuplicate = rapportsTests.stream()
                .anyMatch(existing -> isSameStep(existing, testInfo));

        if (!isDuplicate) {
            testInfo.dateExecution = LocalDateTime.now();
            testInfo.plateforme = PLATFORM;
            rapportsTests.add(testInfo);
        }
    }

    private boolean isSameStep(TestManager existing, TestManager newInfo) {
        return Objects.equals(existing.getNomEtape(), newInfo.getNomEtape()) &&
                Objects.equals(existing.getNomScenario(), newInfo.getNomScenario()) &&
                Objects.equals(existing.getUrl(), newInfo.getUrl());
    }

    public void genererRapport(String nomRapport) {
        String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String fileName = String.format("%s/%s_%s.xlsx", EXCEL_REPORTS_DIR, nomRapport, timeStamp);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Résultats des Tests");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle successStyle = createSuccessStyle(workbook);
            CellStyle failureStyle = createFailureStyle(workbook);

            createHeader(sheet, headerStyle);
            fillData(sheet, successStyle, failureStyle);
            adjustColumns(sheet);
            saveWorkbook(workbook, fileName);
            afficherResume(nomRapport);

        } catch (IOException e) {
            System.err.println("Erreur lors de la génération du rapport Excel: " + e.getMessage());
            e.printStackTrace();
        }
    }

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

    private void createHeader(Sheet sheet, CellStyle headerStyle) {
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
    }

    private void fillData(Sheet sheet, CellStyle successStyle, CellStyle failureStyle) {
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

    private void adjustColumns(Sheet sheet) {
        for (int i = 0; i < 9; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void saveWorkbook(Workbook workbook, String fileName) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(fileName)) {
            workbook.write(outputStream);
            System.out.println("Rapport Excel généré avec succès: " + fileName);
        }
    }

    private void afficherResume(String nomRapport) {
        System.out.println("\nRésumé du rapport: " + nomRapport);
        rapportsTests.stream()
                .filter(info -> info.getNomEtape() != null)
                .forEach(info -> {
                    System.out.println("\n-----------------------------------");
                    System.out.println("Scénario: " + info.getNomScenario());
                    System.out.println("Étape: " + info.getNomEtape());
                    System.out.println("Statut: " + info.getStatut());
                    System.out.println("Plateforme: " + info.getPlateforme());
                    if (info.getResultatAttendu() != null) {
                        System.out.println("Résultat Attendu: " + info.getResultatAttendu());
                    }
                    if (info.getResultatReel() != null) {
                        System.out.println("Résultat Réel: " + info.getResultatReel());
                    }
                    if (info.getUrl() != null) {
                        System.out.println("URL: " + info.getUrl());
                    }
                    if (info.getMessageErreur() != null && !info.getMessageErreur().isEmpty()) {
                        System.out.println("Message d'Erreur: " + info.getMessageErreur());
                    }
                    System.out.println("Date d'Exécution: " +
                            info.dateExecution.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                });
    }
}