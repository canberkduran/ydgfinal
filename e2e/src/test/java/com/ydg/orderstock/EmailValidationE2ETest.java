package com.ydg.orderstock;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class EmailValidationE2ETest extends BaseE2ETest {

    @Test
    void invalidEmailShowsError() {
        openApp();
        click(By.xpath("//button[contains(text(),'Register')]"));
        type(By.cssSelector("[data-testid='register-name']"), "Bad Email");
        type(By.cssSelector("[data-testid='register-email']"), "bad-email");
        type(By.cssSelector("[data-testid='register-password']"), "password123");
        click(By.cssSelector("[data-testid='register-submit']"));

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='toast-error']")));
        String errorText = driver.findElement(By.cssSelector("[data-testid='toast-error']")).getText();
        assertTrue(errorText.contains("Hatali email formati"));
    }
}
