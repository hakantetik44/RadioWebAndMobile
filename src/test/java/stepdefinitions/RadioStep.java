package stepdefinitions;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import pages.RadioPage;
import utils.Driver;
import utils.TestInfo;
import utils.TestReportManager;

import static org.junit.Assert.assertEquals;

public class RadioStep {
    private RadioPage radioPage = new RadioPage();
    private TestInfo testInfo;

    public RadioStep() {
        testInfo = new TestInfo();
    }

    @Then("Ana sayfada olduğumu doğruluyorum")
    public void anaSayfayiDogrula() {
        testInfo.setStepName("Ana sayfa doğrulama");
        try {
            // Mevcut kod...
            testInfo.setStatus("PASSED");
        } catch (Exception e) {
            testInfo.setStatus("FAILED");
            testInfo.setErrorMessage(e.getMessage());
            throw e;
        } finally {
            TestReportManager.getInstance().addTestInfo(testInfo);
        }
    }

    @When("{string} düğmesine tıklarsam")
    public void düğmesineTikla(String aramaButonu) {
        testInfo = new TestInfo();
        testInfo.setStepName("Arama butonu tıklama: " + aramaButonu);

        try {
            radioPage.clickBtnRechercher();
            testInfo.setStatus("PASSED");
        } catch (Exception e) {
            testInfo.setStatus("FAILED");
            testInfo.setErrorMessage(e.getMessage());
            throw e;
        } finally {
            TestReportManager.getInstance().addTestInfo(testInfo);
        }
    }

    @When("Arama alanına {string} yazarsam")
    public void aramaYap(String histoire) {
        testInfo = new TestInfo();
        testInfo.setStepName("Arama yapma");
        testInfo.setExpectedResult("Arama alanına '" + histoire + "' yazılmalı");

        try {
            radioPage.aramaYap(histoire);
            testInfo.setStatus("PASSED");
            testInfo.setActualResult("Arama yapıldı: " + histoire);
        } catch (Exception e) {
            testInfo.setStatus("FAILED");
            testInfo.setErrorMessage(e.getMessage());
            throw e;
        } finally {
            TestReportManager.getInstance().addTestInfo(testInfo);
        }
    }

    @Then("Histoire için sonuçlar gorunmeli")
    public void sonuclariDogrula() throws InterruptedException {
        testInfo = new TestInfo();
        testInfo.setStepName("Sonuçları doğrulama");

        String expectedUrl = "https://www.radiofrance.fr/recherche";
        testInfo.setExpectedResult("URL: " + expectedUrl);

        try {
            String actualUrl = Driver.getCurrentDriver().getCurrentUrl();
            testInfo.setActualResult("URL: " + actualUrl);
            testInfo.setUrl(actualUrl);

            Thread.sleep(3000);
            assertEquals(expectedUrl, actualUrl);

            testInfo.setStatus("PASSED");
        } catch (Exception e) {
            testInfo.setStatus("FAILED");
            testInfo.setErrorMessage(e.getMessage());
            throw e;
        } finally {
            TestReportManager.getInstance().addTestInfo(testInfo);
        }
    }
}