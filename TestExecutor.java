import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class TestExecutor {
    WebDriver driver;
    WebDriverWait wait;
    Actions actions;

    public TestExecutor() {
        driver = new ChromeDriver(new ChromeOptions());
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        actions = new Actions(driver);
    }

    public boolean run(List<TestStep> steps) {
        try {
            for (int i = 0; i < steps.size(); i++) {
                TestStep step = steps.get(i);
                System.out.println("? Step " + (i + 1) + ": Action=" + step.action + " | Value=" + step.value + " | XPath=" + step.xpath);

                switch (step.action.toLowerCase()) {
                    case "openurl":
                        System.out.println("🌐 Opening URL: " + step.value);
                        driver.get(step.value);
                        break;

                    case "click":
                        try {
                            WebElement clickable = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(step.xpath)));
                            clickable.click();
                        } catch (Exception e) {
                            System.out.println("⚠️ Element not clickable, trying JavaScript click...");
                            try {
                                WebElement element = driver.findElement(By.xpath(step.xpath));
                                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
                            } catch (Exception jsEx) {
                                throw new RuntimeException("❌ JavaScript click also failed: " + jsEx.getMessage());
                            }
                        }
                        break;

                    case "type":
                    case "enter":
                        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(step.xpath)));
                        input.clear();
                        input.sendKeys(step.value);
                        break;

                    case "verify":
                        try {
                            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(step.xpath)));
                            System.out.println("✅ Verified visibility of element: " + step.xpath);
                        } catch (Exception e) {
                            throw new RuntimeException("❌ Verification failed: Element not visible or not found for XPath: " + step.xpath);
                        }
                        break;

                    case "verifytext":
                        try {
                            boolean found = wait.until(ExpectedConditions.textToBePresentInElementLocated(By.xpath(step.xpath), step.value));
                            if (!found) {
                                throw new RuntimeException("❌ Text '" + step.value + "' not found in element: " + step.xpath);
                            }
                            System.out.println("✅ Verified text '" + step.value + "' is present in element: " + step.xpath);
                        } catch (Exception e) {
                            throw new RuntimeException("❌ Text verification failed: " + e.getMessage());
                        }
                        break;

                    case "hover":
                        try {
                            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(step.xpath)));
                            WebElement hoverElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(step.xpath)));
                            actions.moveToElement(hoverElement).perform();
                            Thread.sleep(500);
                        } catch (Exception e) {
                            throw new RuntimeException("⚠️ Hover failed: " + e.getMessage());
                        }
                        break;

                    default:
                        throw new RuntimeException("Unknown action: " + step.action);
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Test failed: " + e.getMessage());
            ScreenshotHelper.capture(driver, "failed_step.png");
            return false;
        }

        return true;
    }

    public void close() {
        if (driver != null) {
            driver.quit();
        }
    }
}