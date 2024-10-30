package utils;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TestInfo {
    private String scenarioName;
    private String stepName;
    private String status;
    private String expectedResult;
    private String actualResult;
    private String errorMessage;
    private LocalDateTime executionTime;
    private String platform;
    private String url;
    private String browserLogs;

    public TestInfo() {
        this.executionTime = LocalDateTime.now();
    }

    // Platform setter'ı
    public void setPlatform(String platform) {
        this.platform = platform;
    }

    // Getter metodları
    public String getScenarioName() {
        return scenarioName;
    }

    public String getStepName() {
        return stepName;
    }

    public String getStatus() {
        return status;
    }

    public String getExpectedResult() {
        return expectedResult;
    }

    public String getActualResult() {
        return actualResult;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public LocalDateTime getExecutionTime() {
        return executionTime;
    }

    public String getPlatform() {
        return platform;
    }

    public String getUrl() {
        return url;
    }

    // Setter metodları
    public void setScenarioName(String scenarioName) {
        this.scenarioName = scenarioName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setExpectedResult(String expectedResult) {
        this.expectedResult = expectedResult;
    }

    public void setActualResult(String actualResult) {
        this.actualResult = actualResult;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}