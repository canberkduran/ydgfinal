package com.ydg.orderstock;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class InsufficientBalanceE2ETest extends BaseE2ETest {

    @Test
    void paymentShowsInsufficientBalance() {
        registerAndLogin();

        By expensiveAddButton = By.xpath("//li[.//strong[text()='Expensive Item']]//button[contains(@data-testid,'product-add-to-cart-')]");
        click(expensiveAddButton);

        click(By.cssSelector("[data-testid='cart-checkout']"));
        click(By.cssSelector("[data-testid='pay-submit']"));

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='card-number']")));
        driver.findElement(By.cssSelector("[data-testid='card-number']")).sendKeys("123456");
        driver.findElement(By.cssSelector("[data-testid='card-cvc']")).sendKeys("789");
        driver.findElement(By.cssSelector("[data-testid='pay-confirm']")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='payment-error']")));
        String errorText = driver.findElement(By.cssSelector("[data-testid='payment-error']")).getText();
        assertTrue(errorText.contains("Yetersiz bakiye"));
    }
}
