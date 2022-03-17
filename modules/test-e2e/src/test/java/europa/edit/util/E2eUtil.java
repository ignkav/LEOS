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

import com.google.common.io.Files;
import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.testng.Assert;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
/* 	Author: Satyabrata Das
 * 	Functionality: Utility functions needed for the script execution
 */
@Slf4j
@UtilityClass
@SuppressWarnings({"squid:S2925"})
public class E2eUtil {

    private final Configuration config = new Configuration();
    private final Cryptor td = new Cryptor();

    // To get the timestamp for unique id creation of dossier or other
    public String getDateandTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public void scrollandClick(WebDriver driver, By element) {
        try {
            WebElement webElement = driver.findElement(element);
            JavascriptExecutor executor = (JavascriptExecutor) driver;
            executor.executeScript("arguments[0].scrollIntoView(true);", webElement);
            executor.executeScript("arguments[0].click();", webElement);
            logger.info("Element {} clicked", webElement);
        } catch (StaleElementReferenceException ignore) {
            logger.info("Exception occured while clicking element, but trying click again");
            WebElement webElement = driver.findElement(element);
            JavascriptExecutor executor = (JavascriptExecutor) driver;
            executor.executeScript("arguments[0].scrollIntoView(true);", webElement);
            executor.executeScript("arguments[0].click();", webElement);
            logger.info("Exception occured while clicking element");
        } catch (Exception e) {
            logger.info("Exception occured while clicking element");
            throw e;
        }
    }

    // To take the screenshots of the application in case if validation needed at a particular screen
    // based on the configuration value provided the screenshot will be taking or else wont
    public void takeSnapShot(WebDriver driver, String status) {

        if (config.getProperty("takeScreenshots.pass").contains("TRUE") && status.contains("PASS")) {
            copyScreenshot(driver);
        }

        if (config.getProperty("takeScreenshots.fail").contains("TRUE") && status.contains("FAIL")) {
            copyScreenshot(driver);
        }
    }

    // support function to take screenshot
    private void copyScreenshot(WebDriver driver) {
        TakesScreenshot scrShot = ((TakesScreenshot) driver);
        File srcFile = scrShot.getScreenshotAs(OutputType.FILE);
        String fileName = Constants.RESULTS_LOCATION + File.separator + "Screenshots" + File.separator + "Screenshot_" + getDateandTime() + ".PNG";
        File destFile = new File(fileName);
        boolean copyScreenshots = Boolean.parseBoolean(System.getProperty("copyScreenshots"));
        if (copyScreenshots) {
            TestParameters.getInstance().setScreenshotPath(fileName);
        } else {
            TestParameters.getInstance().setScreenshotPath(destFile.getAbsolutePath());
        }
        try {
            FileUtils.copyFile(srcFile, destFile);
            logger.info("Screenshot captured at " + destFile);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    // Handle the alter pop up during the proxy authentication in firefox browser
/*    public void handleAuthenticationAlert(WebDriver driver) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, 5);
            wait.until(ExpectedConditions.alertIsPresent());
            Alert alert = driver.switchTo().alert();
            alert.sendKeys(td.decrypt(config.getProperty("proxy.user")) + Keys.TAB.toString() + td.decrypt(config.getProperty("proxy.password")));
            alert.accept();
        } catch (Exception e) {
            logger.info("Unable enter the credentials ");
        }
    }*/

    // Definite wait needed at multiple place, used a function instead Thread.sleep method
    public void wait(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void scrollandDoubleClick(WebDriver driver, By element) {
        try {
            WebElement webElement = driver.findElement(element);
            JavascriptExecutor executor = (JavascriptExecutor) driver;
            executor.executeScript("arguments[0].scrollIntoView(true);", webElement);
            executor.executeScript("var evt = document.createEvent('MouseEvents'); evt.initMouseEvent('dblclick',true, true, window, 0, 0, 0, 0, 0, false, false, false, false, 0,null);arguments[0].dispatchEvent(evt);", element);
            logger.info("Element {} double clicked", webElement);
            E2eUtil.takeSnapShot(driver, "PASS");
        } catch (Exception e) {
            logger.info("Exception occured while double clicking element");
            E2eUtil.takeSnapShot(driver, "FAIL");
        }
    }

    public DiskShare smbConnect() {
        DiskShare share;
        try {
            SmbConfig sconfig = SmbConfig.builder()
                    .withTimeout(120, TimeUnit.SECONDS) // Timeout sets Read, Write, and Transact timeouts (default is 60 seconds)
                    .withSoTimeout(180, TimeUnit.SECONDS) // Socket Timeout (default is 0 seconds, blocks forever)
                    .build();
            SMBClient client = new SMBClient(sconfig);
            Connection connection = client.connect(config.getProperty("remote.machine.name"));
            AuthenticationContext ac = new AuthenticationContext(config.getProperty("user.remote.1.name"), td.decrypt(config.getProperty("user.remote.1.pwd")).toCharArray(), config.getProperty("domain"));
            Session session = connection.authenticate(ac);
            share = (DiskShare) session.connectShare(config.getProperty("remote.drive.name"));
            return share;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean unzip(String zipFilePath, String destDirectory, DiskShare share) {
        try {
            ZipInputStream zipIn = null;
            logger.info("before share.folderExists(destDirectory)");
            if (TestParameters.getInstance().getMode().equalsIgnoreCase("remote")) {
                if (!share.folderExists(destDirectory)) {
                    share.mkdir(destDirectory);
                    logger.info("destDir is created");
                }
                com.hierynomus.smbj.share.File file = share.openFile(zipFilePath,
                        EnumSet.of(AccessMask.FILE_READ_DATA),
                        null,
                        SMB2ShareAccess.ALL,
                        SMB2CreateDisposition.FILE_OPEN,
                        null);
                InputStream inputStream = file.getInputStream();
                zipIn = new ZipInputStream(inputStream);
                logger.info("zipIn is assigned");
            }
            if (TestParameters.getInstance().getMode().equalsIgnoreCase("local")) {
                File destDir = new File(destDirectory);
                if (!destDir.exists()) {
                    destDir.mkdir();
                    logger.info("destDir is created");
                }
                zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
                logger.info("zipIn is assigned");
            }
            ZipEntry entry = zipIn.getNextEntry();
            logger.info("entry is assigned");
            while (entry != null) {
                logger.info("entry is not null");
                String filePath = destDirectory + File.separator + entry.getName();
                logger.info("filePath " + filePath);
                if (!entry.isDirectory()) {
                    logger.info("entry.isDirectory()");
                    extractFile(zipIn, filePath, share);
                } else {
                    if (TestParameters.getInstance().getMode().equalsIgnoreCase("remote")) {
                        share.mkdir(filePath);
                    }
                    if (TestParameters.getInstance().getMode().equalsIgnoreCase("local")) {
                        File dir = new File(filePath);
                        dir.mkdirs();
                    }
                }
                zipIn.closeEntry();
                logger.info("zipIn closeEntry");
                entry = zipIn.getNextEntry();
                logger.info("zipIn getNextEntry");
            }
            zipIn.close();
            logger.info("zipIn close");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void extractFile(ZipInputStream zipIn, String filePath, DiskShare share) throws IOException {
        logger.info("entry extractFile");
        BufferedOutputStream bos = null;
        if (TestParameters.getInstance().getMode().equalsIgnoreCase("remote")) {
            com.hierynomus.smbj.share.File file = share.openFile(filePath
                    , EnumSet.of(AccessMask.GENERIC_ALL)
                    , null, SMB2ShareAccess.ALL
                    , SMB2CreateDisposition.FILE_OVERWRITE_IF
                    , null);
            OutputStream outStream = file.getOutputStream();
            bos = new BufferedOutputStream(outStream);
        }
        if (TestParameters.getInstance().getMode().equalsIgnoreCase("local")) {
            bos = new BufferedOutputStream(new FileOutputStream(filePath));
        }
        if (null != bos) {
            logger.info("bos BufferedOutputStream");
            byte[] bytesIn = new byte[Constants.BUFFER_SIZE];
            logger.info("bytesIn BufferedOutputStream");
            int read;
            while ((read = zipIn.read(bytesIn)) != -1) {
                logger.info("zipIn.read");
                bos.write(bytesIn, 0, read);
                logger.info("bos bytesIn");
            }
            logger.info("while completed");
            bos.close();
            logger.info("bos.close()");
        }
    }

    public String findFile(String fileType, String relativePath, DiskShare diskShare) throws IOException {
        long lastModifiedTime = Long.MIN_VALUE;
        String chosenFileName = null;
        try {
            if (TestParameters.getInstance().getMode().equalsIgnoreCase("remote")) {
                if (null != diskShare) {
                    if (null != diskShare.list(config.getProperty("path.remote.download.relative") + Constants.SLASH + relativePath, "*." + fileType)) {
                        for (FileIdBothDirectoryInformation f : diskShare.list(config.getProperty("path.remote.download.relative") + Constants.SLASH + relativePath, "*." + fileType)) {
                            if (f.getLastAccessTime().getWindowsTimeStamp() > lastModifiedTime) {
                                chosenFileName = f.getFileName();
                                lastModifiedTime = f.getLastAccessTime().getWindowsTimeStamp();
                            }
                        }
                    } else {
                        Assert.fail("Issue while finding files from a directory in remote location");
                    }
                } else {
                    try {
                        DiskShare share = smbConnect();
                        if (null != share) {
                            if (null != share.list(config.getProperty("path.remote.download.relative") + Constants.SLASH + relativePath, "*." + fileType)) {
                                for (FileIdBothDirectoryInformation f : share.list(config.getProperty("path.remote.download.relative") + Constants.SLASH + relativePath, "*." + fileType)) {
                                    if (f.getLastAccessTime().getWindowsTimeStamp() > lastModifiedTime) {
                                        chosenFileName = f.getFileName();
                                        lastModifiedTime = f.getLastAccessTime().getWindowsTimeStamp();
                                    }
                                }
                            } else {
                                Assert.fail("Issue while finding files from a directory in remote location");
                            }
                        } else {
                            Assert.fail("DiskShare is coming null");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw e;
                    }
                }
            }
            if (TestParameters.getInstance().getMode().equalsIgnoreCase("local")) {
                File directory = new File(config.getProperty("path.local.download") + Constants.FILE_SEPARATOR + relativePath);
                File[] files = directory.listFiles();
                if (files != null) {
                    for (File file : files) {
                        String extension = Files.getFileExtension(file.getName());
                        if (extension.equalsIgnoreCase(fileType)) {
                            if (file.lastModified() > lastModifiedTime) {
                                chosenFileName = file.getName();
                                lastModifiedTime = file.lastModified();
                            }
                        }
                    }
                } else {
                    Assert.fail("Issue while finding files from a directory in local path");
                }
            }
            if (null != chosenFileName) {
                String FilePath = "";
                if (TestParameters.getInstance().getMode().equalsIgnoreCase("remote")) {
                    FilePath=config.getProperty("path.remote.download") + File.separator + relativePath + Constants.SLASH + chosenFileName;
                }
                if (TestParameters.getInstance().getMode().equalsIgnoreCase("local")) {
                    FilePath=config.getProperty("path.local.download") + File.separator + relativePath + Constants.SLASH + chosenFileName;
                }
                logger.info("FilePath " + FilePath);
                return FilePath;
            }
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw e;
        }
        if(null!=diskShare){
            diskShare.close();
        }
        return null;
    }

    public void findAndUnzipFile(String fileType, String relativePath, String searchFileType) throws IOException {
        long lastModifiedTime = Long.MIN_VALUE;
        String chosenFileName = null;
        DiskShare share = null;
        try {
            if (TestParameters.getInstance().getMode().equalsIgnoreCase("remote")) {
                try {
                    share = smbConnect();
                    if (null != share) {
                        logger.info("share is not null");
                        if (null != share.list(config.getProperty("path.remote.download.relative"), "*." + fileType)) {
                            logger.info("share.list is not null");
                            for (FileIdBothDirectoryInformation f : share.list(config.getProperty("path.remote.download.relative"), "*." + fileType)) {
                                logger.info("FileIdBothDirectoryInformation has value");
                                if (f.getLastAccessTime().getWindowsTimeStamp() > lastModifiedTime) {
                                    logger.info("lastModifiedTime is less");
                                    chosenFileName = f.getFileName();
                                    lastModifiedTime = f.getLastAccessTime().getWindowsTimeStamp();
                                }
                            }
                        } else {
                            Assert.fail("Issue while finding files from a directory in remote location");
                        }
                    } else {
                        Assert.fail("DiskShare is coming null");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    throw e;
                }
            }
            if (TestParameters.getInstance().getMode().equalsIgnoreCase("local")) {
                File directory = new File(config.getProperty("path.local.download"));
                File[] files = directory.listFiles();
                if (files != null) {
                    for (File file : files) {
                        String extension = Files.getFileExtension(file.getName());
                        if (extension.equalsIgnoreCase(fileType)) {
                            if (file.lastModified() > lastModifiedTime) {
                                chosenFileName = file.getName();
                                lastModifiedTime = file.lastModified();
                            }
                        }
                    }
                } else {
                    Assert.fail("Issue while finding files from a directory in local path");
                }
            }
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw e;
        }
        if (null != chosenFileName) {
            logger.info("chosenFileName is not null");
            String zipFilePath = "";
            String destDirectory = "";
            if (TestParameters.getInstance().getMode().equalsIgnoreCase("remote")) {
                zipFilePath = config.getProperty("path.remote.download") + Constants.FILE_SEPARATOR + chosenFileName;
                destDirectory = config.getProperty("path.remote.download") + Constants.FILE_SEPARATOR + relativePath;
            }
            if (TestParameters.getInstance().getMode().equalsIgnoreCase("local")) {
                zipFilePath = config.getProperty("path.local.download") + Constants.FILE_SEPARATOR + chosenFileName;
                destDirectory = config.getProperty("path.local.download") + Constants.FILE_SEPARATOR + relativePath;
            }
            logger.info("zipFilePath " + zipFilePath);
            logger.info("destDirectory " + destDirectory);
            try {
                boolean bool = false;
                if (TestParameters.getInstance().getMode().equalsIgnoreCase("remote")) {
                    bool = unzip(config.getProperty("path.remote.download.relative") + Constants.FILE_SEPARATOR + chosenFileName, config.getProperty("path.remote.download.relative") + Constants.FILE_SEPARATOR + relativePath, share);
                }
                if (TestParameters.getInstance().getMode().equalsIgnoreCase("local")) {
                    bool = unzip(zipFilePath, destDirectory, null);
                }
                if (!bool) {
                    Assert.fail("Error while unzipping file");
                } else {
                    String FileNamePath = E2eUtil.findFile(searchFileType, relativePath, share);
                    if (null != FileNamePath) {
                        System.out.println("legFileNamePath " + FileNamePath);
                    } else {
                        Assert.fail("Unable to find the " + fileType + " file");
                    }
                }
            } catch (Exception | AssertionError ex) {
                ex.printStackTrace();
                throw ex;
            }
        }
        if (null != share) {
            share.close();
        }
    }

    public void scrollElement(WebDriver driver, WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView();", element);
    }

    public void highlightElement(WebDriver driver, WebElement element) {
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
        jsExecutor.executeScript("arguments[0].setAttribute('style', 'border:3px solid yellow;')", element);
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        jsExecutor.executeScript("arguments[0].setAttribute('style', 'border:;')", element);
    }

    /*// to perform the input actions on the angular fields
    public void selectOption(WebDriver driver, By ele) {
        try {
            WebElement element = driver.findElement(ele);
            JavascriptExecutor executor = (JavascriptExecutor) driver;
            executor.executeScript("arguments[0].click();", element);
        } catch (Exception e) {
            logger.info("Exception occured while clicking element");
            logger.error(e.getMessage(), e);
        }
    }

// to perform the input actions on the angular fields
    public void jscriptClick(WebDriver driver, String element, int index) {
        try {
            By ele = By.xpath(String.format(element, index));
            WebElement webelement = driver.findElement(ele);
            JavascriptExecutor executor = (JavascriptExecutor) driver;
            executor.executeScript("arguments[0].click();", webelement);
            logger.info("Element {} clicked", webelement);
        } catch (Exception e) {
            logger.info("Exception occured while clicking element");
            logger.error(e.getMessage(), e);
        }
    }

    public void refreshBrowser(WebDriver driver) {
        try {
            JavascriptExecutor executor = (JavascriptExecutor) driver;
            executor.executeScript("location.reload()");
            logger.info("Browser refreshed");
        } catch (Exception e) {
            logger.info("Exception occured while refreshing the browser");
            logger.error(e.getMessage(), e);
        }
    }*/

    /*public void scrollMouse(WebDriver driver, int height) {
        try {
            JavascriptExecutor executor = (JavascriptExecutor) driver;
            executor.executeScript("window.scrollTo(0," + height + ")");
            logger.info("Window scrolled");
        } catch (Exception e) {
            logger.info("Exception occurred while scrolling the window");
            logger.error(e.getMessage(), e);
        }
    }

    public void scrollElementMouse(WebDriver driver, WebElement ele, int height) {
        try {
            JavascriptExecutor executor = (JavascriptExecutor) driver;
            executor.executeScript("arguments[0].scrollTo(0," + height + ")", ele);
            logger.info("element scrolled");
        } catch (Exception e) {
            logger.info("Exception occurred while scrolling the element");
            logger.error(e.getMessage(), e);
        }
    }*/

   /* // Function to Accept the alert
    public void acceptAlert(WebDriver driver) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, 5);
            wait.until(ExpectedConditions.alertIsPresent());
            Alert alert = driver.switchTo().alert();
            alert.accept();
        } catch (Exception e) {
            logger.info("Unable click ok button");
        }

    }*/

    /*// functions to get document elements from documents section with file name
    public String getFileName(String filePath) {
        String[] file = filePath.split("/");
        return file[file.length - 1];
    }

    public void rightClickOn(WebDriver driver, By element) {
        Actions actions = new Actions(driver);
        WebElement elementLocator = driver.findElement(element);
        actions.contextClick(elementLocator).perform();
    }

    public void mouseOver(WebDriver driver, By element) {
        try {
            Actions actions = new Actions(driver);
            WebElement elementLocator = driver.findElement(element);
            actions.moveToElement(elementLocator).perform();
        } catch (Exception exception) {
            E2eUtil.takeSnapShot(driver, "FAIL");
            logger.info("Action failed during perform" + element);
            logger.error(exception.getMessage(), exception);
            throw new AssertionError("Action failed during perform", exception);
        }
    }


    public void typeText(WebDriver driver, By element, String data) {
        Actions actions = new Actions(driver);
        try {
            WebElement elementLocator = driver.findElement(element);
            actions.sendKeys(data).perform();
            logger.info("Entered text on the {} as {}", element, data);
        } catch (Exception exception) {
            E2eUtil.takeSnapShot(driver, "FAIL");
            logger.info("Action failed during perform" + element);
            logger.error(exception.getMessage(), exception);
            throw new AssertionError("Action failed during perform", exception);
        }

    }

    public String generateRandomNumber() {
        Random rand = new Random();
        return String.valueOf(rand.nextInt(999));
    }

    // To get the current date with desired format
    public String getDateInFormat(String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        LocalDateTime now = LocalDateTime.now();
        return formatter.format(now);
    }

    // To get the current date with desired format
    public String getDateInFormat(String format, int difference) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(format);
        LocalDateTime now = LocalDateTime.now().plusDays(difference);
        return dateFormat.format(now);
    }*/

   /* public static void resizeWindow(WebDriver driver) {
        try {

//            for(int i=0; i<3; i++){
//                driver.findElement(By.tagName("html")).sendKeys(Keys.chord(Keys.CONTROL,Keys.SUBTRACT));
//            }
//            Actions act= new Actions(driver);
*//*            act.sendKeys(Keys.CONTROL).sendKeys(Keys.SUBTRACT).build().perform();
            act.sendKeys(Keys.CONTROL).sendKeys(Keys.SUBTRACT).build().perform();
            act.sendKeys(Keys.CONTROL).sendKeys(Keys.SUBTRACT).build().perform();*//*

//            Action seriesOfActions=act.keyDown(Keys.CONTROL)
//               .keyDown(Keys.SUBTRACT)
//               .keyUp(Keys.SUBTRACT)
//               .keyDown(Keys.SUBTRACT)
//               .keyUp(Keys.SUBTRACT)
//               .keyUp(Keys.CONTROL).build();
//            seriesOfActions.perform();
            *//*Robot robot = new Robot();
            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_MINUS);
            robot.keyRelease(KeyEvent.VK_CONTROL);
            robot.keyRelease(KeyEvent.VK_MINUS);
            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_MINUS);
            robot.keyRelease(KeyEvent.VK_CONTROL);
            robot.keyRelease(KeyEvent.VK_MINUS);
            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_MINUS);
            robot.keyRelease(KeyEvent.VK_CONTROL);
            robot.keyRelease(KeyEvent.VK_MINUS);*//*
     *//*            JavascriptExecutor executor = (JavascriptExecutor)driver;
            executor.executeScript("document.body.style.zoom = '80%';");*//*
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
}