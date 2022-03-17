/*
 * Copyright 2021 European Commission
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package europa.edit.util;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;
import org.testng.ITestResult;
import org.testng.Reporter;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class Common {

    private final int TIME_OUT = 60;
    private final int POLLING_TIME = 1;
    private final String IS_NOT_DISPLAYED = "The following element is NOT displayed: ";
    private final String ELEMENT_NOTPRESENT = "The following element is NOT present: ";
    private final String IS_NOT_DISPLAYED_VERIFIED = "The following element is NOT displayed and NOT verified: ";
    private final String IS_DISPLAYED_AND_CLICKED = "The following element is displayed and clicked: ";
    private final String DOUBLE_CLICKED = "The following element is double clicked: ";
    private final String EXCEPTION_MESSGAE = "The exception occured in finding the following element ";
    private final String EXCEPTION_MESSGAE_ON_FAILURE = "The exception occured during test execution";
    private final String SCROLL_ELEMENT = "arguments[0].scrollIntoView(true);";

    //private final Configuration config = new Configuration();
    //private final int DISPLAY_TIME_OUT = 10;
    //private final String IS_DISPLAYED_VERIFIED = "The following element is displayed and verified: ";
    //private final String IS_TEXT_VERIFIED = "The following text is displayed and verified: ";
    //private final String JAVASCRIPT_CLICK_ELEMENT = "arguments[0].click();";
    //private final String COMMON_FRAME = "GREFFE_FRAMES";

    // Scroll to the element till the element is visible
    public void scrollTo(WebDriver driver, By by) {
        try {
            waitForElement(driver, by);
            if (elementExists(driver, by)) {
                WebElement element = driver.findElement(by);
                ((JavascriptExecutor) driver).executeScript(SCROLL_ELEMENT, element);
                logger.info("Page is scrolled to the element {}", element);
            }
        } catch (Exception e) {
            exceptionReport(driver, by, e);
        }
    }

    public void waitForPageLoadComplete(WebDriver driver, int specifiedTimeout) {
        try {
            //driver.manage().timeouts().implicitlyWait(TIME_OUT, TimeUnit.SECONDS);
            WebDriverWait wait = new WebDriverWait(driver, specifiedTimeout);
            //Wait for Javascript to load
            ExpectedCondition<Boolean> jsLoad = driver1 -> "complete".equals(((JavascriptExecutor) driver)
                    .executeScript("return document.readyState").toString());
            wait.until(jsLoad);
        } catch (Exception e) {
            exceptionReport(driver, e);
        }
    }

    // Function to wait for element to appear
    public void waitForElement(WebDriver driver, By element) {
        try {
            //driver.manage().timeouts().implicitlyWait(TIME_OUT, TimeUnit.SECONDS);
            waitForPageLoadComplete(driver, TIME_OUT);
            //waitForAngularLoad(driver);
            //waitForPendingRequests(driver);
            logger.info("Waiting for {} to display", element);
            E2eUtil.wait(1000);
            WebDriverWait wait = new WebDriverWait(driver, TIME_OUT);
            wait.until(ExpectedConditions.presenceOfElementLocated(element));
        } catch (StaleElementReferenceException staleExcption) {
            E2eUtil.wait(2000);
            //TODO this is workaround solution for flash refresh of the application
            WebDriverWait wait = new WebDriverWait(driver, TIME_OUT);
            wait.until(ExpectedConditions.presenceOfElementLocated(element));
            logger.info("There was a stale element exception, but waited");
        } catch (NoSuchElementException | TimeoutException e) {
            exceptionReport(driver, element, e);
        }
    }

    // Function to wait for element to appear
    public void waitForElementClickable(WebDriver driver, By element) {
        try {
            driver.manage().timeouts().implicitlyWait(TIME_OUT, TimeUnit.SECONDS);
            WebDriverWait wait = new WebDriverWait(driver, TIME_OUT);
            wait.until(ExpectedConditions.elementToBeClickable(driver.findElement(element)));
        } catch (StaleElementReferenceException staleException) {
            E2eUtil.wait(2000);
            //TODO this is workaround solution for flash refresh of the application
            WebDriverWait wait = new WebDriverWait(driver, TIME_OUT);
            wait.until(ExpectedConditions.presenceOfElementLocated(element));
            logger.info("There was a stale element exception, but waited");
        } catch (Exception otherexceptions) {
            exceptionReport(driver, element, otherexceptions);
        }
    }

    public void elementClick(WebDriver driver, By element) {
        try {
            //waitForAngularLoad(driver);
            waitForElementClickable(driver, element);
            if (elementExists(driver, element)) {
                E2eUtil.highlightElement(driver, driver.findElement(element));
                driver.findElement(element).click();
                logger.info(IS_DISPLAYED_AND_CLICKED + element);
                //E2eUtil.takeSnapShot(driver, "PASS");
            } else {
                throw new NoSuchElementException(IS_NOT_DISPLAYED + element);
            }

        } catch (NoSuchElementException e) {
            exceptionReport(driver, element, e);
        } catch (StaleElementReferenceException staleExcption) {
            //TODO this is workaround solution for flash refresh of the application
            E2eUtil.wait(2000);
            WebElement webelement = driver.findElement(element);
            webelement.click();
            logger.info("There was a stale element exception, but clicked");
        } catch (ElementClickInterceptedException elementClickInterceptedException) {
            E2eUtil.scrollandClick(driver, element);
        }
    }

    // Function to verify the element is loaded before entering data
    public void elementEcasSendkeys(WebDriver driver, By by, String data) {
        try {
            if (elementExists(driver, by)) {
                WebElement element = driver.findElement(by);
                E2eUtil.highlightElement(driver, element);
                element.clear();
                element.sendKeys(data);
                logger.info("The data entered at {}", element);
                E2eUtil.takeSnapShot(driver, "PASS");
            } else {
/*                E2eUtil.takeSnapShot(driver, "FAIL");
                Reporter.getCurrentTestResult().setStatus(ITestResult.FAILURE);*/
                throw new NoSuchElementException(IS_NOT_DISPLAYED + by);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
            /*Reporter.getCurrentTestResult().setStatus(ITestResult.FAILURE);
            exceptionReport(driver, element, e);*/
        }
    }

    // Function to keep assertions on particular element.
    public boolean verifyElement(WebDriver driver, By element) {
        waitForElement(driver, element);
        return verifyUIElement(driver, element);
    }

    public boolean verifyUIElement(WebDriver driver, By element) {
        try {
            scrollTo(driver, element);
            if (elementDisplays(driver, element)) {
                E2eUtil.highlightElement(driver, driver.findElement(element));
/*                Reporter.getCurrentTestResult().setStatus(ITestResult.SUCCESS);
                logger.info(IS_DISPLAYED_VERIFIED + element);
                E2eUtil.takeSnapShot(driver, "PASS");*/
                return true;
            } else {
                Reporter.getCurrentTestResult().setStatus(ITestResult.FAILURE);
                logger.info(IS_NOT_DISPLAYED_VERIFIED + element);
                E2eUtil.takeSnapShot(driver, "FAIL");
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    // Element exists or not
    public boolean elementExists(WebDriver driver, By element) {
        //driver.manage().timeouts().implicitlyWait(DISPLAY_TIME_OUT, TimeUnit.SECONDS);
        return elementExistsWithOutwait(driver, element);
    }

    // Element exists or not
    public boolean elementExistsWithOutwait(WebDriver driver, By element) {
        // Tries 3 times in case of StaleElementReferenceException
        for (int i = 0; true; i++) {
            try {
                return driver.findElement(element).isEnabled();
            } catch (StaleElementReferenceException e) {
                if (i > 2) {
                    throw e;
                }
            } catch (NoSuchElementException e) {
                return false;
            }
        }
    }

    // Element displayed or not
    public Boolean elementDisplays(WebDriver driver, By element) {
        try {
            //driver.manage().timeouts().implicitlyWait(DISPLAY_TIME_OUT, TimeUnit.SECONDS);
            return driver.findElement(element).isDisplayed();
        } catch (Exception e) {
            return false;
        }

    }

    public String getElementText(WebDriver driver, By by) {
        try {
            return driver.findElement(by).getText();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    //Get Text of the element
    public String getElementText(WebElement element) {
        try {
            return element.getText();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    //Get Element Attribute Value
    public String getElementAttributeValue(WebDriver driver, By by) {
        try {
            return driver.findElement(by).getAttribute("value");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    //Get Element Attribute InnerText
    public String getElementAttributeInnerText(WebDriver driver, By element) {
        try {
            return driver.findElement(element).getAttribute("innerText");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    // Take screenshot if scenario fails and stop execution
    private void exceptionReport(WebDriver driver, By element, Exception e) {
        E2eUtil.takeSnapShot(driver, "FAIL");
        Reporter.getCurrentTestResult().setStatus(ITestResult.FAILURE);
        logger.info(EXCEPTION_MESSGAE + element);
        logger.error(e.getMessage(), e);
        throw new AssertionError(EXCEPTION_MESSGAE, e);
    }

    // Take screenshot if scenario fails and stop execution
    private void exceptionReport(WebDriver driver, Exception e) {
        E2eUtil.takeSnapShot(driver, "FAIL");
        logger.info(EXCEPTION_MESSGAE_ON_FAILURE);
        logger.error(e.getMessage(), e);
        throw new AssertionError(EXCEPTION_MESSGAE_ON_FAILURE, e);
    }

    public boolean verifyIsElementSelected(WebDriver driver, By by) {
        return driver.findElement(by).isSelected();
    }

    public boolean verifyElementIsEnabled(WebDriver driver, By element) {
        return elementExistsWithOutwait(driver, element);
    }

    public void isElementDisabled(WebDriver driver, By element) {
        for (int i = 0; true; i++) {
            try {
                driver.findElement(element).isEnabled();
                return;
            } catch (StaleElementReferenceException e) {
                if (i > 2) {
                    throw e;
                }
            } catch (NoSuchElementException e) {
                return;
            }
        }
    }

    public void verifyStringContainsText(WebDriver driver, By element) {
        for (int i = 0; true; i++) {
            try {
                driver.findElement(element).getText();
                return;
            } catch (StaleElementReferenceException e) {
                if (i > 2) {
                    throw e;
                }
            } catch (NoSuchElementException e) {
                return;
            }
        }
    }

    public void doubleClick(WebDriver driver, By element) {
        try {
            waitForElementClickable(driver, element);
            if (elementExists(driver, element)) {
                Actions actions = new Actions(driver);
                WebElement elementLocator = driver.findElement(element);
                actions.doubleClick(elementLocator).perform();
                logger.info(DOUBLE_CLICKED + element);
            } else {
                E2eUtil.takeSnapShot(driver, "FAIL");
                throw new NoSuchElementException(ELEMENT_NOTPRESENT + element);
            }

        } catch (NoSuchElementException e) {
            exceptionReport(driver, element, e);
        } catch (StaleElementReferenceException staleExcption) {
            //TODO this is workaround solution for flash refresh of the application
            E2eUtil.wait(2000);
            Actions actions = new Actions(driver);
            WebElement elementLocator = driver.findElement(element);
            actions.doubleClick(elementLocator).perform();
            logger.info("There was a stale element exception, but clicked");
        } catch (ElementClickInterceptedException elementClickInterceptedException) {
            E2eUtil.scrollandDoubleClick(driver, element);
        }
    }

    public boolean verifyElementNotPresent(WebDriver driver, By locator) {
        WebElement element;
        Wait<WebDriver> wait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(30))
                .pollingEvery(Duration.ofSeconds(2))
                .ignoring(NoSuchElementException.class);
        try {
            element = wait.until(driver1 -> driver1.findElement(locator));
        } catch (NoSuchElementException | TimeoutException e) {
            return true;
        }
        return null == element;
    }

    public void elementActionClick(WebDriver driver, By locator) {
        WebElement element;
        Actions act;
        Wait<WebDriver> wait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(30))
                .pollingEvery(Duration.ofSeconds(2))
                .ignoring(NoSuchElementException.class);
        try {
            element = wait.until(driver1 -> driver1.findElement(locator));
        } catch (NoSuchElementException | TimeoutException e) {
            e.printStackTrace();
            throw e;
        }
        if (null != element) {
            try {
                act = new Actions(driver);
                act.moveToElement(element).click().perform();
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }
    }

    public static boolean selectTextThroughDoubleClick(WebDriver driver, By contextOfTheProposalText) {
        try {
            WebElement element = driver.findElement(contextOfTheProposalText);
            /*Integer width = element.getSize().getWidth();
            Actions act = new Actions(driver);
            act.moveByOffset(element.getLocation().getX() + width,
                    element.getLocation().getY() + width).click();
            act.build().perform();
            act.moveByOffset(width/2,0).clickAndHold().moveByOffset(width,0).release().build().perform();*/
            Actions act = new Actions(driver);
            act.doubleClick(element).build().perform();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static void selectText(WebDriver driver, By locator) {
        try {
            WebElement element = driver.findElement(locator);
/*            JavascriptExecutor js=(JavascriptExecutor) driver;
            String mouseOverScript = "if(document.createEvent){var evObj = document.createEvent('MouseEvents');evObj.initEvent('mouseover', true, false); arguments[0].dispatchEvent(evObj);} else if(document.createEventObject) { arguments[0].fireEvent('onmouseover');}";
            js.executeScript(mouseOverScript, element);*/
            Dimension size = element.getSize();
            int width = size.getWidth();
            Actions action = new Actions(driver);
            action.clickAndHold(element)
                    .moveToElement(element, -width / 2, 0)
                    .build().perform();
            action.release().build().perform();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public WebElement waitForElementTobePresent(WebDriver driver, By by) {
        Wait<WebDriver> wait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(TIME_OUT))
                .pollingEvery(Duration.ofSeconds(POLLING_TIME))
                .ignoring(Exception.class);
        return wait.until(driver1 -> driver1.findElement(by));
    }

    public boolean waitForElementTobeDisPlayed(WebDriver driver, By by) {
        Wait<WebDriver> wait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(TIME_OUT))
                .pollingEvery(Duration.ofSeconds(POLLING_TIME))
                .ignoring(Exception.class);
        return wait.until(driver1 -> driver1.findElement(by).isDisplayed());
    }

    public boolean waitUnTillElementIsNotPresent(WebDriver driver, By by) {
        try {
            Wait<WebDriver> wait = new FluentWait<>(driver)
                    .withTimeout(Duration.ofSeconds(TIME_OUT))
                    .pollingEvery(Duration.ofSeconds(POLLING_TIME));
            wait.until(ExpectedConditions.invisibilityOfElementLocated(by));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

     /*    // function to wait for the element to disappear
    public boolean waitForInvisibility(WebDriver driver, By element) {
        try {
            E2eUtil.wait(1000);
            WebDriverWait wait = new WebDriverWait(driver, TIME_OUT);
            return wait.until(ExpectedConditions.invisibilityOf(driver.findElement(element)));

        } catch (Exception e) {
            return false;
        }

    }*/

/*    public void waitUntilJQueryReady(WebDriver driver) {
        try {
            driver.manage().timeouts().implicitlyWait(TIME_OUT, TimeUnit.SECONDS);
            WebDriverWait wait = new WebDriverWait(driver, TIME_OUT);
            JavascriptExecutor jsExec = (JavascriptExecutor) driver;
            ExpectedCondition<Boolean> jQueryLoad = unused -> {
                Boolean jQueryDefined = (Boolean) jsExec.executeScript("return typeof jQuery != 'undefined'");
                if (!jQueryDefined) {
                    return true;
                }
                return ((Long) jsExec
                        .executeScript("return jQuery.active") == 0);
            };
            wait.until(jQueryLoad);

        } catch (Exception e) {
            exceptionReport(driver, e);
        }
    }*/

    //Wait for Angular Load
/*    public void waitForAngularLoad(WebDriver driver) {
        try {
            //driver.manage().timeouts().implicitlyWait(TIME_OUT, TimeUnit.SECONDS);
            WebDriverWait wait = new WebDriverWait(driver, TIME_OUT);
            JavascriptExecutor jsExec = (JavascriptExecutor) driver;
            //First check that ANGULAR is defined on the page. If it is, then wait ANGULAR
            Boolean angularUnDefined = (Boolean) jsExec.executeScript("return typeof angular != 'undefined'");
            if (angularUnDefined) {
                String angularReadyScript = "return angular.element(document).injector().get('$http').pendingRequests.length === 0";

                //Wait for ANGULAR to load
                ExpectedCondition<Boolean> angularLoad = driver1 -> Boolean.valueOf(((JavascriptExecutor) driver)
                        .executeScript(angularReadyScript).toString());
                wait.until(angularLoad);
            }
        } catch (JavascriptException js) {
            js.printStackTrace();
        } catch (Exception e) {
            exceptionReport(driver, e);
        }
    }*/

    // Function to wait for element to appear
/*    public void waitForElementVisible(WebDriver driver, By element) {
        try {
            driver.manage().timeouts().implicitlyWait(TIME_OUT, TimeUnit.SECONDS);
            WebDriverWait wait = new WebDriverWait(driver, TIME_OUT);
            wait.until(ExpectedConditions.presenceOfElementLocated(element));
        } catch (StaleElementReferenceException staleExcption) {
            //TODO this is workaround solution for flash refresh of the application
            WebDriverWait wait = new WebDriverWait(driver, TIME_OUT);
            wait.until(ExpectedConditions.presenceOfElementLocated(element));
            logger.info("There was a stale element exception, but waited");
        } catch (NoSuchElementException | TimeoutException e) {
            exceptionReport(driver, element, e);
        }
    }*/

    // Scroll to the element till the element is visible
/*    private void scrollWithOutWait(WebDriver driver, By webelement) {
        try {
            waitForPageLoadComplete(driver, DISPLAY_TIME_OUT);
            if (elementExists(driver, webelement)) {
                WebElement element = driver.findElement(webelement);
                ((JavascriptExecutor) driver).executeScript(SCROLL_ELEMENT, element);
                logger.info("Page is scrolled to the element {}", element);
            }
        } catch (Exception e) {
            exceptionReport(driver, webelement, e);
        }
    }*/

/*    // Function to wait for the loading of the application after save is clicked
    public void waitForUploadElement(WebDriver driver, By element) {
        try {
            int count = 0;
            while (count <= TIME_OUT) {
                if (driver.findElement(element).isEnabled()) {
                    break;
                } else {
                    logger.info("Waiting for upload element {}", element);
                    driver.manage().timeouts().implicitlyWait(TIME_OUT, TimeUnit.SECONDS);
                    count = count + 1;
                }
            }
        } catch (Exception e) {
            exceptionReport(driver, element, e);
        }
    }*/

    /*// Function to verify element before click and after verified click method called
    public void elementClickMultipleTimes(WebDriver driver, By element, String annexNumber) {
        try {
            int annexNum = Integer.parseInt(annexNumber.trim());
            while (annexNum > 1) {
                if (elementExists(driver, element)) {
                    E2eUtil.wait(1000);
                    driver.findElement(element).click();
                    logger.info(IS_DISPLAYED_AND_CLICKED + element);
                    E2eUtil.takeSnapShot(driver, "PASS");
                } else {
                    logger.info(IS_NOT_DISPLAYED + element);
                    E2eUtil.takeSnapShot(driver, "FAIL");
                }
                annexNum--;
            }

        } catch (NoSuchElementException e) {
            exceptionReport(driver, element, e);
        } catch (StaleElementReferenceException staleExcption) {
            //TODO this is workaround solution for flash refresh of the application
            WebElement webelement = driver.findElement(element);
            webelement.click();
            logger.info("There was a stale element exception, but clicked");
        } catch (WebDriverException webdriverexception) {
            WebElement webelement = driver.findElement(element);
            JavascriptExecutor executor = (JavascriptExecutor) driver;
            executor.executeScript(JAVASCRIPT_CLICK_ELEMENT, webelement);
        }
    }

    // Function to verify element before click and after verified click method called
    public void elementspecialClick(WebDriver driver, By ele) {
        try {
            if (elementExists(driver, ele)) {
                WebElement element = driver.findElement(ele);
                Actions actions = new Actions(driver);
                actions.moveToElement(element);
                actions.click();
                actions.build().perform();
                logger.info(IS_DISPLAYED_AND_CLICKED + element);
                E2eUtil.takeSnapShot(driver, "PASS");
            } else {
                throw new NoSuchElementException(IS_NOT_DISPLAYED + ele);
            }

        } catch (Exception e) {
            exceptionReport(driver, ele, e);
        }

    }
*/
    // Function to verify the element is loaded before entering data
 /*   public void elementSendkeys(WebDriver driver, By element, String data) {
        try {
            waitForPageLoadComplete(driver, TIME_OUT);
            if (elementExists(driver, element)) {
                driver.findElement(element).sendKeys(data);
                logger.info("The {} entered at {}", data, element);
                E2eUtil.takeSnapShot(driver, "PASS");
            } else {
                throw new NoSuchElementException(IS_NOT_DISPLAYED + element);
            }

        } catch (StaleElementReferenceException stale) {
            //TODO this is workaround solution for flash refresh of the application
            waitForPageLoadComplete(driver, TIME_OUT);
            driver.findElement(element).sendKeys(data);
            logger.info("Stale Exception, The {} entered at {}", data, element);
        } catch (Exception e) {
            exceptionReport(driver, element, e);
        }
    }*/

/*    // Function to enter data in dyanmic input (Change of Translation request screen)
    public void elementDynamicSendkeys(WebDriver driver, String element, String data, String dataInput) {
        By ele = By.xpath(String.format(element, data));
        elementSendkeys(driver, ele, dataInput);
    }*/

    // Function to clear the text field
/*    public void inputClear(WebDriver driver, By element) {
        try {
            waitForPageLoadComplete(driver, TIME_OUT);
            if (elementExists(driver, element)) {
                driver.findElement(element).clear();
                logger.info("The input field is cleared");
                E2eUtil.takeSnapShot(driver, "PASS");
            } else {
                throw new NoSuchElementException(IS_NOT_DISPLAYED + element);
            }

        } catch (StaleElementReferenceException stale) {
            //TODO this is workaround solution for flash refresh of the application
            waitForPageLoadComplete(driver, TIME_OUT);
            driver.findElement(element).clear();
            logger.info("The input field is cleared");
        } catch (NoSuchElementException e) {
            exceptionReport(driver, element, e);
        }
    }*/

    // Function to keep assertions on particular element.
/*    public void verifyStaleElement(WebDriver driver, By element) {
        try {
            verifyElements(driver, element);
        } catch (StaleElementReferenceException staleExcption) {
            //TODO this is workaround solution for flash refresh of the application
            WebDriverWait wait = new WebDriverWait(driver, TIME_OUT);
            wait.until(ExpectedConditions.presenceOfElementLocated(element));
            logger.info("There was a stale element exception, but waited");
            verifyElements(driver, element);
        } catch (NoSuchElementException e) {
            exceptionReport(driver, element, e);
        }
    }*/

/*    // Function to verify Toast element
    public void verifyToastBar(WebDriver driver, By element) {
        try {
            if (elementExists(driver, element)) {
                Reporter.getCurrentTestResult().setStatus(ITestResult.SUCCESS);
                logger.info("The notification toast {} is displayed", element);
                E2eUtil.takeSnapShot(driver, "PASS");
            }
            *//*else if (elementExists(driver, CreateDossierPage.ERROR_TOAST_LABEL)) {
                Reporter.getCurrentTestResult().setStatus(ITestResult.FAILURE);
                logger.error("The notification toast {} is NOT displayed", element);
                throw new NoSuchElementException("Red toast is displayed with error message");
            }*//*
        } catch (StaleElementReferenceException stale) {
            //TODO this is workaround solution for flash refresh of the application
            waitForPageLoadComplete(driver, TIME_OUT);
            verifyToastBar(driver, element);
            logger.info("Test is waiting for page load");
        } catch (NoSuchElementException e) {
            exceptionReport(driver, element, e);
        }
    }*/

    /*    private void verifyElements(WebDriver driver, By element) {
            waitForPageLoadComplete(driver, TIME_OUT);
            try {
                if (elementDisplays(driver, element)) {
                    Reporter.getCurrentTestResult().setStatus(ITestResult.SUCCESS);
                    logger.info(IS_DISPLAYED_VERIFIED + element);
                    E2eUtil.takeSnapShot(driver, "PASS");
                } else {
                    Reporter.getCurrentTestResult().setStatus(ITestResult.FAILURE);
                    throw new NoSuchElementException(IS_NOT_DISPLAYED_VERIFIED + element);
                }
            } catch (NoSuchElementException e) {
                exceptionReport(driver, element, e);
            }
        }*/

    // Function to keep assertions on particular element.
/*    public boolean verifyElementText(WebDriver driver, By element, String text) {
        try {
            if (driver.findElement(element).getText().contains(text)) {
                Reporter.getCurrentTestResult().setStatus(ITestResult.SUCCESS);
                logger.info(IS_DISPLAYED_VERIFIED + "{} with text {}", element, text);
                E2eUtil.takeSnapShot(driver, "PASS");
                return true;

            } else {
                Reporter.getCurrentTestResult().setStatus(ITestResult.FAILURE);
                throw new NoSuchElementException(IS_NOT_DISPLAYED_VERIFIED + element);
            }
        } catch (Exception e) {
            exceptionReport(driver, element, e);
            return false;
        }
    }*/

    // Function to keep assertions on particular element.
/*    public void verifyElementValue(WebDriver driver, By element, String text) {
        try {

            String theTextIWant = ((JavascriptExecutor) driver).executeScript("return arguments[0].value;", driver.findElement(element)).toString();
            if (theTextIWant.contains(text)) {
                Reporter.getCurrentTestResult().setStatus(ITestResult.SUCCESS);
                logger.info(IS_DISPLAYED_VERIFIED + "{} with text {}", element, text);
                E2eUtil.takeSnapShot(driver, "PASS");

            } else {
                Reporter.getCurrentTestResult().setStatus(ITestResult.FAILURE);
                throw new NoSuchElementException(IS_NOT_DISPLAYED_VERIFIED + element);
            }
        } catch (Exception e) {
            exceptionReport(driver, element, e);
        }

    }*/

    // Function to keep assertions on particular element.
/*    public boolean verifyElementWithText(WebDriver driver, String element, String data) {
        By ele = By.xpath(String.format(element, data));
        try {
            scrollWithOutWait(driver, ele);
            return verifyStaleElement(driver, ele);
        } catch (Exception e) {
            exceptionReport(driver, ele, e);
            return false;
        }

    }*/

    // Function to keep assertions on particular element.
/*    public void verifyElementWithMultipleText(WebDriver driver, String element, String data1, String data2) {
        By ele = By.xpath(String.format(element, data1, data2));
        try {
            scrollWithOutWait(driver, ele);
            verifyStaleElement(driver, ele);
        } catch (Exception e) {
            exceptionReport(driver, ele, e);
        }
    }*/

/*    // Function to keep assertions on particular element with dynamic attribute
    public void verifyElementTextWithAttribute(WebDriver driver, By element, String text, String attribute) {
        try {
            String actualVal = driver.findElement(element).getAttribute(attribute);
            if (actualVal.contains(text) || text.contains(actualVal)) {
                Reporter.getCurrentTestResult().setStatus(ITestResult.SUCCESS);
                logger.info(IS_TEXT_VERIFIED + text);
                E2eUtil.takeSnapShot(driver, "PASS");

            } else {
                logger.info("The value {} is NOT verified", text);
                Reporter.getCurrentTestResult().setStatus(ITestResult.FAILURE);
                throw new NoSuchElementException("The value " + text + " is NOT verified");
            }
        } catch (Exception e) {
            logger.info("Exception occured while fetching the attribute value");
            exceptionReport(driver, element, e);
        }

    }

    // Function to keep assertions on dynamic element with dynamic attribute
    public void verifyDynamicElementTextWithAttribute(WebDriver driver, String elementString, String data, String text, String attribute) {
        try {
            By element = By.xpath(String.format(elementString, data));
            String actualVal = driver.findElement(element).getAttribute(attribute);
            if (actualVal.contains(text) || text.contains(actualVal)) {
                Reporter.getCurrentTestResult().setStatus(ITestResult.SUCCESS);
                logger.info(IS_TEXT_VERIFIED + text);
                E2eUtil.takeSnapShot(driver, "PASS");

            } else {
                logger.info("The value {} is NOT verified", text);
                Reporter.getCurrentTestResult().setStatus(ITestResult.FAILURE);
                throw new NoSuchElementException("The value " + text + " is NOT verified");
            }
        } catch (Exception e) {
            logger.info("Exception occured while fetching the attribute value");
            exceptionReport(driver, elementString, e);
        }

    }*/

    // verify element not exist
/*    public void verifyElementNotExists(WebDriver driver, By element) {
        try {
            if (elementExistsWithOutwait(driver, element)) {
                Reporter.getCurrentTestResult().setStatus(ITestResult.FAILURE);
                logger.info("The element {} is appeared as not expected", element);
                throw new TimeoutException("Element Found");
            } else {
                Reporter.getCurrentTestResult().setStatus(ITestResult.SUCCESS);
                logger.info("The element {} is not appeared as expected", element);
            }

        } catch (Exception e) {
            logger.info("Exception occured while verifying the non-existence");
            exceptionReport(driver, element, e);
        }
    }*/

    // verify element not exist
 /*   public void verifyDynamicElementNotExists(WebDriver driver, String element, String data) {
        try {
            By ele = By.xpath(String.format(element, data));
            if (!(elementExistsWithOutwait(driver, ele))) {
                Reporter.getCurrentTestResult().setStatus(ITestResult.SUCCESS);
                logger.info("The element {} is not appeared as expected", ele);
            } else {
                Reporter.getCurrentTestResult().setStatus(ITestResult.FAILURE);
                logger.info("The element {} is appeared as not expected", element);
                throw new TimeoutException("Element Found");
            }
        } catch (Exception e) {
            logger.info("Exception occured while verifying the non-existence");
            exceptionReport(driver, element, e);
        }
    }
*/
    // Function to keep assertions on particular element.
    /*public void verifyDynamicElementText(WebDriver driver, String element, String data, String verifyText) {
        By ele = By.xpath(String.format(element, data));
        try {
            scrollTo(driver, ele);
            waitForElement(driver, ele);
            if (driver.findElement(ele).getText().contains(verifyText)) {
                Reporter.getCurrentTestResult().setStatus(ITestResult.SUCCESS);
                logger.info(IS_TEXT_VERIFIED + verifyText);
                E2eUtil.takeSnapShot(driver, "PASS");

            } else {
                Reporter.getCurrentTestResult().setStatus(ITestResult.FAILURE);
                throw new NoSuchElementException("Unable to verify the document status as " + verifyText);
            }
        } catch (Exception e) {
            exceptionReport(driver, ele, e);
        }

    }*/

    // Function to keep assertions on particular element.
/*    private void verifyDynamicElementValue(WebDriver driver, String element, int data, String verifyText) {
        By ele = By.xpath(String.format(element, data));
        try {
            if (elementExists(driver, ele)) {
                scrollTo(driver, ele);
                E2eUtil.wait(1000);
                if (driver.findElement(ele).getAttribute("value").contains(verifyText)) {
                    Reporter.getCurrentTestResult().setStatus(ITestResult.SUCCESS);
                    logger.info("The document status {} is verified", verifyText);
                    E2eUtil.takeSnapShot(driver, "PASS");

                } else {
                    Reporter.getCurrentTestResult().setStatus(ITestResult.FAILURE);
                    throw new NoSuchElementException("Unable to verify the document status as " + verifyText);

                }
            }

        } catch (NoSuchElementException e) {
            exceptionReport(driver, ele, e);
        }
    }*/

    // Function to handle to selection option from dropdown
/*    public void selectOption(WebDriver driver, String element, String data) {
        try {
            By ele = By.xpath(String.format(element, data));
            scrollTo(driver, ele);
            waitForElement(driver, ele);
            elementClick(driver, ele);
            logger.info("Option {} selected at {}", data, element);
        } catch (Exception e) {
            logger.error("Exception occurred while selecting option the element {}", element);
            exceptionReport(driver, element, e);
        }
    }*/

/*    // Function to handle to selection option from dropdown in Consultation application
    public void selectDynamicOption(WebDriver driver, String element, String data) {
        try {
            By ele = By.xpath(String.format(element, data));
            waitForElement(driver, ele);
            elementClick(driver, ele);
            logger.info("Option {} selected at {}", data, element);
        } catch (Exception e) {
            logger.error("Exception occurred while selecting option the element {}", element);
            exceptionReport(driver, element, e);
        }

    }

    // Function to handle to selection option from dropdown in Consultation application
    public void selectDynamicOptionByIndex(WebDriver driver, String element, int index) {
        try {
            By ele = By.xpath(String.format(element, index));
            waitForElement(driver, ele);
            elementClick(driver, ele);
            logger.info("Option {} selected at {}", index, element);
        } catch (Exception e) {
            logger.error("Exception occurred while selecting option the element {}", element);
            exceptionReport(driver, element, e);
        }

    }


    // Function to handle to selection option from dropdown
    public void selectEgreffOption(WebDriver driver, String element, String data) {
        waitForPageLoadComplete(driver, TIME_OUT);
        try {
            By ele = By.xpath(String.format(element, data));
            scrollWithOutWait(driver, ele);
            elementClick(driver, ele);
            logger.info("Option {} selected at {}", data, element);
        } catch (Exception e) {
            logger.error("Exception occurred while selecting option the element {}", element);
            exceptionReport(driver, e);
        }

    }

    public void clickAllElements(WebDriver driver, By obj) {
        List<WebElement> elements = driver.findElements(obj);
        logger.info("the size of all elements is {}", elements.size());
        int i = 1;
        try {
            for (WebElement ele : elements) {
                ((JavascriptExecutor) driver).executeScript(SCROLL_ELEMENT, ele);
                //verifyDeadLinesDates(driver, i);
                ((JavascriptExecutor) driver).executeScript(JAVASCRIPT_CLICK_ELEMENT, ele);
                E2eUtil.wait(1000);
                i++;
            }
        } catch (Exception e) {
            logger.error("Exception in validation of request");
            exceptionReport(driver, obj, e);
        }
    }*/

/*    public void fileUpload(WebDriver driver, String filepath, By element) {

        // get file form the classpath
        String uploadFile = null;
        try {
            if (config.getProperty("mode").equalsIgnoreCase("Remote")) {
                uploadFile = config.getProperty("upload.download.path") + filepath;
            } else {
                uploadFile = new URI(Common.class.getClassLoader().getResource("europa/edit/uploads/" + filepath).getFile()).getPath();
            }
            if (elementExists(driver, element)) {
                WebElement upload = driver.findElement(element);
                upload.sendKeys(uploadFile);
                logger.info("The document {} is uploaded at {}", uploadFile, element);
            }
        } catch (URISyntaxException e) {
            logger.error("The uri for the filepath {} is not valid", filepath);
            exceptionReport(driver, element, e);
        } catch (StaleElementReferenceException stale) {
            //TODO this is workaround solution for flash refresh of the application
            WebElement upload = driver.findElement(element);
            upload.sendKeys(uploadFile);
            logger.info("Stale exception occurred but tried to uploaded doc {}", uploadFile);
        } catch (Exception e) {
            logger.error("Exception occured, The element {} is NOT displayed or filepath {} is not correct", element, uploadFile);
            exceptionReport(driver, element, e);
        }
    }*/

    /*   public void multiplefileUpload(WebDriver driver, String files, String filepath, By element) {

           // get file form the classpath

           String[] fileNames = files.split(",");
           String uploadFile = "";
           List<String> paths = new ArrayList<>();
           try {

               for (String filename : fileNames) {

                   if (config.getProperty("mode").equalsIgnoreCase("remote")) {
                       paths.add(config.getProperty("upload.download.path") + filepath + filename);

                   } else {
                       paths.add(new URI(Common.class.getClassLoader().getResource("europa/edit/uploads/" + filepath + filename).getFile()).getPath());
                   }

               }
               uploadFile = String.join("\n", paths);
               if (elementExists(driver, element)) {
                   WebElement upload = driver.findElement(element);
                   upload.sendKeys(uploadFile);
                   logger.info("The document {} is uploaded at {}", uploadFile, element);
               }

           } catch (URISyntaxException e) {
               logger.error("The uri for the filepath {} is not valid", filepath);
               exceptionReport(driver, element, e);
           } catch (StaleElementReferenceException stale) {
               //TODO this is workaround solution for flash refresh of the application
               WebElement upload = driver.findElement(element);
               upload.sendKeys(uploadFile);
               logger.info("Stale exception occurred but tried to uploaded doc {}", uploadFile);
           } catch (Exception e) {
               logger.error("Exception occured, The element {} is NOT displayed or filepath {} is not correct", element, uploadFile);
               exceptionReport(driver, element, e);
           }

       }*/

    public void setValueToElementAttribute(WebDriver driver, By by, String val) {
        try {
            WebElement ele = driver.findElement(by);
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].value=arguments[1];", ele, val);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public String getInnerHTML(WebDriver driver, By by) {
        try {
            return driver.findElement(by).getAttribute("innerHTML");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static String getValueFromElementAttribute(WebDriver driver, By by, String attribute) {
        try {
            return driver.findElement(by).getAttribute(attribute);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /* // Element displayed or if not displayed throws an exception
    public void elementVerifywithString(WebDriver driver, String element, String data) {
        By ele = By.xpath(String.format(element, data));
        try {
            scrollWithOutWait(driver, ele);
            if (elementDisplays(driver, ele)) {
                Reporter.getCurrentTestResult().setStatus(ITestResult.SUCCESS);
                logger.info(IS_DISPLAYED_VERIFIED + element);
                E2eUtil.takeSnapShot(driver, "PASS");
            }
        } catch (Exception e) {
            exceptionReport(driver, ele, e);

        }

    }

    public void performDragAndDrop(WebDriver driver, By fromElement, By targetElement) {
        Actions actions = new Actions(driver);
        try {
            WebElement element = driver.findElement(fromElement);
            WebElement target = driver.findElement(targetElement);
            //Building a drag and drop action
            Action dragAndDrop = actions.clickAndHold(element)
                    .moveToElement(target)
                    .build();
            //Performing the drag and drop action
            dragAndDrop.perform();
            actions.release(driver.findElement(By.xpath("//div[@class='dndPlaceholder']"))).build().perform();
            E2eUtil.wait(5000);
            logger.info("Drag and drop performed successfully");
        } catch (Exception e) {
            exceptionReport(driver, fromElement, e);

        }

    }*/

    //Select option from drop down
/*    public void selectOptionfromDropdown(WebDriver driver, By element, String option) {
        try {
            Select drpCountry = new Select(driver.findElement(element));
            drpCountry.selectByVisibleText(option);
        } catch (Exception e) {
            exceptionReport(driver, element, e);
        }
    }*/

    //Select option from drop down by value
/*    public void selectOptionfromDropdownByValue(WebDriver driver, By element, String value) {
        try {
            Select drpCountry = new Select(driver.findElement(element));
            drpCountry.selectByValue(value);
        } catch (Exception e) {
            exceptionReport(driver, element, e);
        }
    }*/

    /*    //////////////////////////////////////////////////// EGREFFE FUNCTIONS ///////////////////////////////
        // Function to keep assertions on particular element at egreffe page
        public void verifyLegacyElementText(WebDriver driver, By element, String verifyText, String frameName) {
            switchToAFrame(driver, COMMON_FRAME);
            switchToAFrame(driver, frameName);
            verifyElementText(driver, element, verifyText);
            driver.switchTo().defaultContent();
        }

        // Function to keep assertions on particular element.
        public void verifyLegacyElement(WebDriver driver, By element, String frameName) {
            switchToAFrame(driver, COMMON_FRAME);
            switchToAFrame(driver, frameName);
            verifyStaleElement(driver, element);
            driver.switchTo().defaultContent();
        }

        // Function to wait for legacy elements
        public void waitForLegacyElement(WebDriver driver, By element, String frameName) {
            switchToAFrame(driver, COMMON_FRAME);
            switchToAFrame(driver, frameName);
            waitForElementVisible(driver, element);
            driver.switchTo().defaultContent();
        }

        // Function to assert with replacing test on the element.
        public void verifyLegacyElementWithText(WebDriver driver, String element, String data, String frameName) {
            switchToAFrame(driver, COMMON_FRAME);
            switchToAFrame(driver, frameName);
            verifyElementWithText(driver, element, data);
            driver.switchTo().defaultContent();
        }

        // Function to assert with replacing test on the element.
        public void verifyLegacyElementWithMultipleText(WebDriver driver, String element, String data1, String data2, String frameName) {
            switchToAFrame(driver, COMMON_FRAME);
            switchToAFrame(driver, frameName);
            verifyElementWithMultipleText(driver, element, data1, data2);
            driver.switchTo().defaultContent();
        }

        // Function to click on Egreffe elements
        public void clickLegacyElement(WebDriver driver, By element, String frameName) {
            switchToAFrame(driver, COMMON_FRAME);
            switchToAFrame(driver, frameName);
            waitForPageLoadComplete(driver, TIME_OUT);
            elementClick(driver, element);
            driver.switchTo().defaultContent();
        }

        // Function to Egreffe element without switch to default
        public void clickLegacyElementNoSwitch(WebDriver driver, By element, String frameName) {
            switchToAFrame(driver, COMMON_FRAME);
            switchToAFrame(driver, frameName);
            elementClick(driver, element);
        }

        // Function to keep select in Legacy
        public void selectLegacyElement(WebDriver driver, By element, String frameName, String option) {
            switchToAFrame(driver, COMMON_FRAME);
            switchToAFrame(driver, frameName);
            selectOptionfromDropdown(driver, element, option);
            driver.switchTo().defaultContent();
        }

        // Function to keep select in Legacy by Value of the option
        public void selectLegacyElementByValue(WebDriver driver, By element, String frameName, String value) {
            switchToAFrame(driver, COMMON_FRAME);
            switchToAFrame(driver, frameName);
            selectOptionfromDropdownByValue(driver, element, value);
            driver.switchTo().defaultContent();
        }

        // Function to send keys in text fields in Legacy
        public void sendkeysLegacyElement(WebDriver driver, By element, String frameName, String data) {
            switchToAFrame(driver, COMMON_FRAME);
            switchToAFrame(driver, frameName);
            inputClear(driver, element);
            elementSendkeys(driver, element, data);
            driver.switchTo().defaultContent();
        }

        // Function to scroll to Legacy element
        public void scrollToLegacyElement(WebDriver driver, By element, String frameName) {
            switchToAFrame(driver, COMMON_FRAME);
            switchToAFrame(driver, frameName);
            scrollWithOutWait(driver, element);
            driver.switchTo().defaultContent();
        }

        // Function to verify exist of Legacy element
        public Boolean existsofLegacyElement(WebDriver driver, By element, String frameName) {
            switchToAFrame(driver, COMMON_FRAME);
            switchToAFrame(driver, frameName);
            Boolean exists = elementExists(driver, element);
            driver.switchTo().defaultContent();
            return exists;
        }

        // Function to verify non existance of Dynamic Legacy element
        public void nonExistsOfDyanmicLegacyElement(WebDriver driver, String element, String frameName, String data) {
            switchToAFrame(driver, COMMON_FRAME);
            switchToAFrame(driver, frameName);
            verifyDynamicElementNotExists(driver, element, data);
            driver.switchTo().defaultContent();
        }

        // Function to verify non existance of Legacy element
        public void nonExistsOfLegacyElement(WebDriver driver, By element, String frameName) {
            switchToAFrame(driver, COMMON_FRAME);
            switchToAFrame(driver, frameName);
            verifyElementNotExists(driver, element);
            driver.switchTo().defaultContent();
        }

        // Function to verify existance of Legacy element
        public void verifyDyanmicLegacyElement(WebDriver driver, String element, String frameName, String data, String verifyText) {
            switchToAFrame(driver, COMMON_FRAME);
            switchToAFrame(driver, frameName);
            By ele = By.xpath(String.format(element, data));
            try {
                if (driver.findElement(ele).getText().contains(verifyText)) {
                    Reporter.getCurrentTestResult().setStatus(ITestResult.SUCCESS);
                    logger.info(IS_TEXT_VERIFIED + verifyText);
                    E2eUtil.takeSnapShot(driver, "PASS");

                } else {
                    Reporter.getCurrentTestResult().setStatus(ITestResult.FAILURE);
                    throw new NoSuchElementException("Unable to verify the document status as " + verifyText);
                }
            } catch (Exception e) {
                exceptionReport(driver, ele, e);
            }
            driver.switchTo().defaultContent();
        }

        //Function to select dynamic Legacy element
        public void selectDynamicLegacyElement(WebDriver driver, String element, String frameName, String data) {
            switchToAFrame(driver, COMMON_FRAME);
            switchToAFrame(driver, frameName);
            waitForPageLoadComplete(driver, TIME_OUT);
            selectEgreffOption(driver, element, data);
            driver.switchTo().defaultContent();
        }

        //Function to to upload a file in Legacy
        public void fileUploadLegacyElement(WebDriver driver, By element, String data, String frameName) {
            switchToAFrame(driver, COMMON_FRAME);
            switchToAFrame(driver, frameName);
            fileUpload(driver, data, element);
            driver.switchTo().defaultContent();
        }

        // upload consultation documents
        public void uploadConsultationDocument(WebDriver driver, By element, String filepath) {
            try {
                // get file form the classpath
                //TODO change the access file method
                var uploadFile = Common.class.getClassLoader().getResource("europa/edit/uploads/" + filepath).getFile();
                if (config.getProperty("mode").equalsIgnoreCase("remote")) {
                    uploadFile = config.getProperty("upload.download.path") + filepath;
                }

                // disable the click event on an `<input>` file
                ((JavascriptExecutor) driver).executeScript(
                        "HTMLInputElement.prototype.click = function() {                     " +
                                "  if(this.type !== 'file') HTMLElement.prototype.click.call(this);  " +
                                "};                                                                  ");

                elementClick(driver, element);
                E2eUtil.wait(200);

                // assign the file to the `<input>`
                driver.findElement(By.cssSelector("input[type=file]"))
                        .sendKeys(uploadFile);
    *//*            waitForElementClickable(driver, ConsultationPage.CONFIRM_BUTTON);
            elementClick(driver, ConsultationPage.CONFIRM_BUTTON);*//*

        } catch (Exception e) {
            logger.info("Upload pop up is NOt displayed");
            exceptionReport(driver, element, e);
        }

    }

    // Set the consultation id after saving the details
    public void setConsultationDossierID(WebDriver driver, By element) {
        try {
            waitForElement(driver, element);
            String[] dossierID = driver.findElement(element).getText().split(" ");
            //TestParameters.getInstance().setDossierId(dossierID[1]);
        } catch (Exception e) {
            exceptionReport(driver, element, e);
        }
    }

    //Get element attribute value
    public String getElementAttributeValue(WebDriver driver, By element, String attribute) {
        return driver.findElement(element).getAttribute(attribute);
    }*/

/*    // Handling and switching to pop-up windows
    public void switchToWindow(WebDriver driver) {
        String parentWindow = driver.getWindowHandle();
        Set<String> allWindows = driver.getWindowHandles();
        for (String curWindow : allWindows) {
            if (!curWindow.equals(parentWindow)) {
                driver.switchTo().window(curWindow);
                driver.manage().window().maximize();
                break;
            }
        }
    }

    // Switching a Specific frame on the fly
    public void switchToAFrame(WebDriver driver, String FrameName) {
        try {
            E2eUtil.wait(1000);
            driver.switchTo().frame(FrameName);
        } catch (NoSuchElementException | NoSuchFrameException e) {
            exceptionReport(driver, "Frame-" + FrameName, e);
        }

    }

    // Switching a Specific frame on the fly with indes
    public void switchToAFrameByIndex(WebDriver driver, int Index, String Title) {
        try {
            E2eUtil.wait(1000);
            driver.switchTo().frame(Index);
        } catch (NoSuchElementException | NoSuchFrameException e) {
            exceptionReport(driver, "Frame-" + Title, e);
        }

    }

    // Handling and switching back to main windows
    public void swicthToMainWindow(WebDriver driver) {
        Set<String> allWindows = driver.getWindowHandles();
        for (String curWindow : allWindows) {
            driver.switchTo().window(curWindow);
        }
    }

    public void closeWindow(WebDriver driver) {
        try {
            driver.close();
            logger.info("Window closed successfully");
        } catch (Exception e) {
            exceptionReport(driver, e);
        }

    }*/

    // Take screenshot if scenario fails and stop execution
/*    private void exceptionReport(WebDriver driver, String element, Exception e) {
        E2eUtil.takeSnapShot(driver, "FAIL");
        logger.info(EXCEPTION_MESSGAE + element);
        logger.error(e.getMessage(), e);
        throw new AssertionError(EXCEPTION_MESSGAE, e);
    }*/

    /*public boolean verifyTextDoesNotContainInPage(WebDriver driver, By element) {
        for (int i = 0; true; i++) {
            try {
                return !driver.findElement(element).isDisplayed();
            } catch (StaleElementReferenceException e) {
                if (i > 2) {
                    throw e;
                }
            } catch (NoSuchElementException e) {
                return false;
            }
        }
    }*/

    /*public static void mouseHoverElementAndClick(WebDriver driver, By locator) {
        {
            WebElement element = null;
            Wait<WebDriver> wait = new FluentWait<WebDriver>(driver)
                    .withTimeout(60, TimeUnit.SECONDS)
                    .pollingEvery(5, TimeUnit.SECONDS)
                    .ignoring(NoSuchElementException.class);
            try {
                element = wait.until(new Function<WebDriver, WebElement>() {
                    public WebElement apply(WebDriver driver) {
                        return driver.findElement(locator);
                    }
                });
            } catch (NoSuchElementException | TimeoutException e) {
                e.printStackTrace();
                throw e;
            }
            if (null != element) {
                try {
                    Actions act = new Actions(driver);
                    act.moveToElement(element).click().perform();
                } catch (Exception e) {
                    e.printStackTrace();
                    throw e;
                }
            }
        }
    }*/
}