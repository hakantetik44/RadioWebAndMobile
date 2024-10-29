package runner;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        plugin = {
                "pretty", // Konsolda daha iyi görüntüleme için
                "json:target/cucumber.json", // JSON raporu
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm" // Allure raporu
        },
        features = "src/test/resources/features", // Özellik dosyalarının yolu
        glue = "stepdefinitions", // Adım tanımlamaları için paket
        tags = "", // Belirli etiketleri çalıştırmak için
        dryRun = false // Testlerin çalıştırılacağını belirtir
)
public class TestRunner {
}
