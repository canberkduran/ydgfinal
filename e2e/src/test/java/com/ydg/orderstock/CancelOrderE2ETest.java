package com.ydg.orderstock;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CancelOrderE2ETest extends BaseE2ETest {

    @Test
    void cancelRestoresStock() {
        registerAndLogin();

        By cancelableAddButton = By.xpath("//li[.//strong[text()='Cancelable Item']]//button[contains(@data-testid,'product-add-to-cart-')]");
        click(cancelableAddButton);

        click(By.cssSelector("[data-testid='cart-checkout']"));

        By cancelButton = By.xpath("//button[contains(text(),'Cancel')]");
        click(cancelButton);

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("[data-testid^='order-status-']"), "CANCELLED"));

        String stockText = driver.findElement(By.xpath("//li[.//strong[text()='Cancelable Item']]//div[contains(text(),'Stock')]")).getText();
        assertTrue(stockText.contains("2"));
    }
}
