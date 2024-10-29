package runner;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features", // Özellik dosyalarının yolu
        glue = "your.step.definitions.package", // Adım tanımları için doğru paket
        plugin = {"pretty", "json:target/cucumber.json", "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"}
)
public class TestRunner {
}
