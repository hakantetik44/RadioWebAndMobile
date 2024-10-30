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
    private static TestReportManager instance;
    private List<TestInfo> testInfoList;
    private static final String REPORT_DIR = "target/test-reports";

    private TestReportManager() {
        testInfoList = new ArrayList<>();
        createReportDirectory();
    }

    private void createReportDirectory() {
        try {
            Path reportPath = Paths.get(REPORT_DIR);
            if (!Files.exists(reportPath)) {
                Files.createDirectories(reportPath);
            }
        } catch (IOException e) {
            System.err.println("Rapor dizini oluşturulamadı: " + e.getMessage());
        }
    }

    public static TestReportManager getInstance() {
        if (instance == null) {
            instance = new TestReportManager();
        }
        return instance;
    }

    public void addTestInfo(TestInfo testInfo) {
        if (testInfo != null) {
            testInfoList.add(testInfo);
        }
    }

    public void generateReport(String testName) {
        String timeStamp = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
                .format(java.time.LocalDateTime.now());
        String fileName = String.format("%s/%s_%s.xlsx", REPORT_DIR, testName, timeStamp);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Test Results");

            // Başlık stili
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Başlık satırı
            Row headerRow = sheet.createRow(0);
            String[] columns = {
                    "Senaryo", "Adım", "Durum", "Platform",
                    "Beklenen Sonuç", "Gerçek Sonuç",
                    "Hata Mesajı", "URL", "Zaman"
            };

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 6000);
            }

            // Test verilerini doldur
            int rowNum = 1;
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (TestInfo info : testInfoList) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(info.getScenarioName());
                row.createCell(1).setCellValue(info.getStepName());
                row.createCell(2).setCellValue(info.getStatus());
                row.createCell(3).setCellValue(info.getPlatform());
                row.createCell(4).setCellValue(info.getExpectedResult());
                row.createCell(5).setCellValue(info.getActualResult());
                row.createCell(6).setCellValue(info.getErrorMessage());
                row.createCell(7).setCellValue(info.getUrl());
                row.createCell(8).setCellValue(
                        info.getExecutionTime().format(dtf)
                );

                // Başarısız testler için kırmızı arka plan
                if ("FAILED".equals(info.getStatus())) {
                    CellStyle failedStyle = workbook.createCellStyle();
                    failedStyle.setFillForegroundColor(IndexedColors.ROSE.getIndex());
                    failedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    row.getCell(2).setCellStyle(failedStyle);
                }
            }

            // Raporu kaydet
            try (FileOutputStream outputStream = new FileOutputStream(fileName)) {
                workbook.write(outputStream);
                System.out.println("Test raporu oluşturuldu: " + fileName);
            }

        } catch (IOException e) {
            System.err.println("Rapor oluşturma hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void clearTestInfo() {
        testInfoList.clear();
    }

    public List<TestInfo> getTestInfoList() {
        return new ArrayList<>(testInfoList);
    }
}