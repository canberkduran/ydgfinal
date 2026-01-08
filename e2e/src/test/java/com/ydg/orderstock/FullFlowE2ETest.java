package com.ydg.orderstock;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class FullFlowE2ETest extends BaseE2ETest {

    @Test
    void registerLoginCheckoutPayShipFlow() {
        registerAndLogin();

        click(By.cssSelector("[data-testid^='product-add-to-cart-']"));
        click(By.cssSelector("[data-testid='cart-checkout']"));

        WebElement payButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-testid='pay-submit']")));
        payButton.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='card-number']")));
        driver.findElement(By.cssSelector("[data-testid='card-number']")).sendKeys("123456");
        driver.findElement(By.cssSelector("[data-testid='card-cvc']")).sendKeys("789");
        driver.findElement(By.cssSelector("[data-testid='pay-confirm']")).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("[data-testid^='order-status-']"), "PAID"));

        WebElement shipButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Ship')]")));
        shipButton.click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("[data-testid^='order-status-']"), "SHIPPED"));
        String statusText = driver.findElement(By.cssSelector("[data-testid^='order-status-']")).getText();
        assertTrue(statusText.contains("SHIPPED"));
    }
}
