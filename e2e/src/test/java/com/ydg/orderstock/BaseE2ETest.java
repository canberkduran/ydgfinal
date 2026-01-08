package com.ydg.orderstock;

import java.net.URL;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class BaseE2ETest {
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected String baseUrl;
    protected String seleniumUrl;

    @BeforeEach
    void setup() throws Exception {
        baseUrl = System.getenv().getOrDefault("FRONTEND_URL", "http://frontend");
        seleniumUrl = System.getenv().getOrDefault("SELENIUM_URL", "http://selenium:4444/wd/hub");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        driver = new RemoteWebDriver(new URL(seleniumUrl), options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @AfterEach
    void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    protected void openApp() {
        driver.get(baseUrl);
    }

    protected void registerAndLogin() {
        String email = "user-" + UUID.randomUUID() + "@example.com";
        openApp();
        click(By.xpath("//button[contains(text(),'Register')]"));
        type(By.cssSelector("[data-testid='register-name']"), "Test User");
        type(By.cssSelector("[data-testid='register-email']"), email);
        type(By.cssSelector("[data-testid='register-password']"), "password123");
        click(By.cssSelector("[data-testid='register-submit']"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//h2[contains(text(),'Products')]")));
    }

    protected void loginWith(String email, String password) {
        openApp();
        click(By.xpath("//button[contains(text(),'Login')]"));
        type(By.cssSelector("[data-testid='login-email']"), email);
        type(By.cssSelector("[data-testid='login-password']"), password);
        click(By.cssSelector("[data-testid='login-submit']"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//h2[contains(text(),'Products')]")));
    }

    protected void click(By selector) {
        wait.until(ExpectedConditions.elementToBeClickable(selector)).click();
    }

    protected void type(By selector, String value) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(selector));
        el.clear();
        el.sendKeys(value);
    }
}
