package com.ydg.orderstock;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class StockInsufficientE2ETest extends BaseE2ETest {

    @Test
    void checkoutShowsErrorWhenStockInsufficient() {
        registerAndLogin();

        By limitedAddButton = By.xpath("//li[.//strong[text()='Limited Widget']]//button[contains(@data-testid,'product-add-to-cart-')]");
        click(limitedAddButton);
        click(limitedAddButton);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='toast-error']")));
        String errorText = driver.findElement(By.cssSelector("[data-testid='toast-error']")).getText();
        assertTrue(errorText.contains("Stok yetersiz"));
    }
}
