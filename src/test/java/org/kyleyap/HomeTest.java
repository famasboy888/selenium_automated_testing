package org.kyleyap;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HomeTest {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static ChromeOptions chromeOptions;

    //Extent Report
    private static ExtentTest test;
    private static ExtentReports report;

    //Screenshot
    private static String screenshotDir;

    @BeforeAll
    public static void startup() {
        chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--headless=new");
//        driver = new ChromeDriver(chromeOptions);
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(500));

        //Extent Report
        ExtentSparkReporter sparkReporter = new ExtentSparkReporter("/target/ExtentReports.html");
        report = new ExtentReports();
        report.attachReporter(sparkReporter);

        //Screenshot setup
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        screenshotDir = "/target/screenshots/" + timestamp;
        try {
            Files.createDirectories(Paths.get(screenshotDir)); // Create directory for screenshots
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @Order(1)
    void HomeIntialLoadTest() {
        try {
            test = report.createTest("HomeIntialLoadTest");
            test.log(Status.INFO, "<h2><u><b>Test case HomeIntialLoadTest started</b></u></h2>");
            driver.manage().window().maximize();
            driver.get("http://localhost:3000/employees");

            WebElement title = driver.findElement(By.xpath("//a[normalize-space()='Employee Management System']"));
            WebElement addButton = driver.findElement(By.xpath("//button[normalize-space()='Add Employee']"));
            WebElement EmployeeListText = driver.findElement(By.xpath("//h2[normalize-space()='List of Employees']"));

            // Table
            WebElement table = driver.findElement(By.xpath("//table[contains(@class,'table table-hover table-bordered')]"));
            List<WebElement> headers = table.findElements(By.xpath("//table[contains(@class,'table table-hover table-bordered')]/thead/tr/th"));
            String[] tableHeaders = {"Id", "First Name", "Last Name", "Mail", "Actions"};
            WebElement emptyText = table.findElement(By.xpath("//tbody//tr//td"));


            Assertions.assertEquals("Employee Management System", title.getText(), "Title text does not match");
            test.log(Status.PASS, "Title text is correct");

            Assertions.assertEquals("Add Employee", addButton.getText(), "Add Button text does not match");
            test.log(Status.PASS, "Add Button text is correct");

            Assertions.assertEquals("List of Employees", EmployeeListText.getText(), "Employee List Text does not match");
            test.log(Status.PASS, "Employee List Text is correct");

            Assertions.assertTrue(table.isDisplayed(), "Table is not displayed");
            test.log(Status.PASS, "Table is displayed");

            for (int i = 0; i < headers.size(); i++) {
                Assertions.assertEquals(headers.get(i).getText(), tableHeaders[i], "Table header does not match");
                test.log(Status.PASS, "Table header " + tableHeaders[i] + " is correct");
            }
            Assertions.assertTrue(emptyText.isDisplayed(), "Empty text is not displayed");
            test.log(Status.PASS, "Empty record is displayed", MediaEntityBuilder.createScreenCaptureFromPath(takeScreenshot(driver, "HomeIntialLoadTest")).build());

            test.log(Status.INFO, "Test case HomeIntialLoadTest Ended");
        } catch (Exception e) {
            test.log(Status.FAIL, "<div style='color:red;'>" + e.getMessage() + "</div>", MediaEntityBuilder.createScreenCaptureFromPath(takeScreenshot(driver, "HomeIntialLoadTest")).build());
            throw e;
        }

    }

    @Test
    @Order(2)
    void HomeTransitionToAddEmployeeViewTest() {

        try {
            test = report.createTest("HomeTransitionToAddEmployeeViewTest");
            test.log(Status.INFO, "<h2><u><b>Test case HomeTransitionToAddEmployeeViewTest started</b></u></h2>");

            driver.manage().window().maximize();
            driver.get("http://localhost:3000/employees");

            WebElement addButton = driver.findElement(By.xpath("//button[normalize-space()='Add Employee']"));
            addButton.click();

            Assertions.assertEquals("http://localhost:3000/add-employee", driver.getCurrentUrl());
            test.log(Status.PASS, "Transitioned correctly to Add Employee View", MediaEntityBuilder.createScreenCaptureFromPath(takeScreenshot(driver, "HomeTransitionToAddEmployeeViewTest")).build());

            test.log(Status.INFO, "Test case HomeTransitionToAddEmployeeViewTest Ended");
        } catch (Exception e) {
            test.log(Status.FAIL, e.getMessage(), MediaEntityBuilder.createScreenCaptureFromPath(takeScreenshot(driver, "HomeTransitionToAddEmployeeViewTest")).build());
            throw e;
        }

    }

    @Test
    @Order(3)
    void AddNewEmployeeRecordTest() {
        try {
            test = report.createTest("AddNewEmployeeRecordTest");
            test.log(Status.INFO, "<h2><u><b>Test case AddNewEmployeeRecordTest started</b></u></h2>");

            addEmployeeHelper("Nancy", "Drew", "nancy@gmail.com");
            addEmployeeHelper("Polly", "Morph", "poli@gmail.com");
            addEmployeeHelper("Test", "Tester", "test@gmail.com");

            //Back to Home page after insertion
            wait.until(ExpectedConditions.urlToBe("http://localhost:3000/employees"));

            //Get table
            WebElement table = driver.findElement(By.xpath("//table[contains(@class,'table table-hover table-bordered')]"));
            List<WebElement> dataRow = table.findElements(By.xpath("//tbody/tr"));
            WebElement dataRowOneFname = dataRow.getFirst().findElement(By.xpath("(//td)[1]"));
            WebElement dataRowOneLname = dataRow.getFirst().findElement(By.xpath("(//td)[2]"));
            WebElement dataRowOneEmai = dataRow.getFirst().findElement(By.xpath("(//td)[3]"));

            Assertions.assertEquals("http://localhost:3000/employees", driver.getCurrentUrl());
            test.log(Status.PASS, "Current URL is correct");

            Assertions.assertEquals(3, dataRow.size());
            test.log(Status.PASS, "Table row size is 3");

            Assertions.assertEquals("Nancy", dataRowOneFname.getText());
            test.log(Status.PASS, "First name is correct");

            Assertions.assertEquals("Drew", dataRowOneLname.getText());
            test.log(Status.PASS, "Last name is correct");

            Assertions.assertEquals("nancy@gmail.com", dataRowOneEmai.getText());
            test.log(Status.PASS, "Email is correct");
            test.log(Status.PASS, "Employee Added successfully", MediaEntityBuilder.createScreenCaptureFromPath(takeScreenshot(driver, "AddNewEmployeeRecordTest")).build());

            test.log(Status.INFO, "Test case AddNewEmployeeRecordTest Ended");
        } catch (Exception e) {
            test.log(Status.FAIL, e.getMessage(), MediaEntityBuilder.createScreenCaptureFromPath(takeScreenshot(driver, "AddNewEmployeeRecordTest")).build());
            throw e;
        }
    }

    @Test
    @Order(4)
    void HomeTransitionToUpdateEmployeeViewTest() {
        try {
            test = report.createTest("HomeTransitionToUpdateEmployeeViewTest");
            test.log(Status.INFO, "<h2><u><b>Test case HomeTransitionToUpdateEmployeeViewTest started</b></u></h2>");

            driver.manage().window().maximize();
            driver.get("http://localhost:3000/employees");

            WebElement table = driver.findElement(By.xpath("//table[contains(@class,'table table-hover table-bordered')]"));
            List<WebElement> dataRow = table.findElements(By.xpath("//tbody/tr"));
            WebElement updateBtn = dataRow.getFirst().findElement(By.xpath("//td[4]/button[1]"));

            updateBtn.click();

            wait.until(ExpectedConditions.urlToBe("http://localhost:3000/edit-employee/1"));
            WebElement textTitle = driver.findElement(By.xpath("//h2[normalize-space()='Update Employee']"));


            WebElement inputFname = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@data-testid='form-field-firstname']")));
            WebElement inputLname = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@data-testid='form-field-lastname']")));
            WebElement inputEmail = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@data-testid='form-field-email']")));

            wait.until(ExpectedConditions.attributeToBeNotEmpty(inputFname, "value"));
            wait.until(ExpectedConditions.attributeToBeNotEmpty(inputLname, "value"));
            wait.until(ExpectedConditions.attributeToBeNotEmpty(inputEmail, "value"));

            Assertions.assertEquals("Nancy", inputFname.getAttribute("value"));
            test.log(Status.PASS, "First name is correct");

            Assertions.assertEquals("Drew", inputLname.getAttribute("value"));
            test.log(Status.PASS, "Last name is correct");

            Assertions.assertEquals("nancy@gmail.com", inputEmail.getAttribute("value"));
            test.log(Status.PASS, "Email is correct");

            Assertions.assertEquals("Update Employee", textTitle.getText());
            test.log(Status.PASS, "Title is correct");
            test.log(Status.PASS, "Transitioned to Employee Update View", MediaEntityBuilder.createScreenCaptureFromPath(takeScreenshot(driver, "HomeTransitionToUpdateEmployeeViewTest")).build());

            test.log(Status.INFO, "Test case HomeTransitionToUpdateEmployeeViewTest Ended");
        } catch (Exception e) {
            test.log(Status.FAIL, e.getMessage(), MediaEntityBuilder.createScreenCaptureFromPath(takeScreenshot(driver, "HomeTransitionToUpdateEmployeeViewTest")).build());
            throw e;
        }

    }

    @Test
    @Order(5)
    void UpdateEmployeeTest() {
        try {
            test = report.createTest("UpdateEmployeeTest");
            test.log(Status.INFO, "<h2><u><b>Test case UpdateEmployeeTest started</b></u></h2>");

            test.log(Status.INFO, "Before Update Employee", MediaEntityBuilder.createScreenCaptureFromPath(takeScreenshot(driver, "BeforeUpdateEmployeeTest")).build());

            driver.manage().window().maximize();

            WebElement inputFname = driver.findElement(By.xpath("//input[@data-testid='form-field-firstname']"));
            WebElement inputLname = driver.findElement(By.xpath("//input[@data-testid='form-field-lastname']"));
            WebElement inputEmail = driver.findElement(By.xpath("//input[@data-testid='form-field-email']"));
            WebElement addEmployeeBtn = driver.findElement(By.xpath("//button[normalize-space()='Submit']"));
            inputFname.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
            inputLname.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
            inputEmail.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
            inputFname.sendKeys("Drewberry");
            inputLname.sendKeys("Morpheus");
            inputEmail.sendKeys("morpheus@gmail.com");

            test.log(Status.INFO, "Modify Employee details", MediaEntityBuilder.createScreenCaptureFromPath(takeScreenshot(driver, "ModifyEmployeeDetail")).build());
            addEmployeeBtn.click();

            wait.until(ExpectedConditions.urlToBe("http://localhost:3000/employees"));
            WebElement table = driver.findElement(By.xpath("//table[contains(@class,'table table-hover table-bordered')]"));
            List<WebElement> dataRow = table.findElements(By.xpath("//tbody/tr"));
            WebElement dataRowOneFname = dataRow.getFirst().findElement(By.xpath("(//td)[1]"));
            WebElement dataRowOneLname = dataRow.getFirst().findElement(By.xpath("(//td)[2]"));
            WebElement dataRowOneEmai = dataRow.getFirst().findElement(By.xpath("(//td)[3]"));


            Assertions.assertEquals("http://localhost:3000/employees", driver.getCurrentUrl());
            test.log(Status.PASS, "Current URL is correct");

            Assertions.assertEquals(3, dataRow.size());
            test.log(Status.PASS, "Table row size is correct");

            Assertions.assertEquals("Drewberry", dataRowOneFname.getText());
            test.log(Status.PASS, "First name is correct");

            Assertions.assertEquals("Morpheus", dataRowOneLname.getText());
            test.log(Status.PASS, "Last name is correct");

            Assertions.assertEquals("morpheus@gmail.com", dataRowOneEmai.getText());
            test.log(Status.PASS, "Email is correct");
            test.log(Status.PASS, "Updated Employee Employee", MediaEntityBuilder.createScreenCaptureFromPath(takeScreenshot(driver, "UpdateEmployeeTest")).build());

            test.log(Status.INFO, "Test case HomeTransitionToUpdateEmployeeViewTest Ended");
        } catch (Exception e) {
            test.log(Status.FAIL, e.getMessage(), MediaEntityBuilder.createScreenCaptureFromPath(takeScreenshot(driver, "UpdateEmployeeTest")).build());
            throw e;
        }

    }

    @Test
    @Order(6)
    void DeleteEmployeeTest() {
        try {
            test = report.createTest("DeleteEmployeeTest");
            test.log(Status.INFO, "<h2><u><b>Test case DeleteEmployeeTest started</b></u></h2>");

            driver.manage().window().maximize();
            driver.get("http://localhost:3000/employees");
            WebElement table = driver.findElement(By.xpath("//table[contains(@class,'table table-hover table-bordered')]"));
            List<WebElement> dataRow = table.findElements(By.xpath("//tbody/tr"));
            WebElement deleteBtn = dataRow.getFirst().findElement(By.xpath("//td[4]/button[2]"));

            test.log(Status.INFO, "Before Delete Employee", MediaEntityBuilder.createScreenCaptureFromPath(takeScreenshot(driver, "BeforeDeleteEmployeeTest")).build());

            deleteBtn.click();

            List<WebElement> element = wait.until(ExpectedConditions.numberOfElementsToBe(By.xpath("//tbody/tr"), 2));
            Assertions.assertEquals(2, element.size());
            test.log(Status.PASS, "Deleted Employee", MediaEntityBuilder.createScreenCaptureFromPath(takeScreenshot(driver, "DeleteEmployeeTest")).build());

            test.log(Status.INFO, "Test case DeleteEmployeeTest Ended");
        } catch (Exception e) {
            test.log(Status.FAIL, e.getMessage(), MediaEntityBuilder.createScreenCaptureFromPath(takeScreenshot(driver, "DeleteEmployeeTest")).build());
            throw e;
        }
    }

    @AfterAll
    public static void cleanup() throws InterruptedException {
        if (driver != null) {
            driver.quit();
            report.flush();
        }
    }

    public static String takeScreenshot(WebDriver driver, String testName) {

        File source = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

        String destination = Paths.get(screenshotDir, testName + "_" + UUID.randomUUID() + ".PNG").toString();
        try {
            Files.copy(source.toPath(), Paths.get(destination), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return destination;
    }


    public void addEmployeeHelper(String firstname, String lastname, String email) {
        driver.manage().window().maximize();
        driver.get("http://localhost:3000/add-employee");
        WebElement firstNameText = driver.findElement(By.xpath("//input[@data-testid='form-field-firstname']"));
        firstNameText.sendKeys(firstname);

        WebElement lastNameText = driver.findElement(By.xpath("//input[@data-testid='form-field-lastname']"));
        lastNameText.sendKeys(lastname);

        WebElement emailText = driver.findElement(By.xpath("//input[@data-testid='form-field-email']"));
        emailText.sendKeys(email);

        test.log(Status.INFO, "Populate Employee Added Form", MediaEntityBuilder.createScreenCaptureFromPath(takeScreenshot(driver, "addEmployeeHelper")).build());

        WebElement addEmployeeBtn = driver.findElement(By.xpath("//button[normalize-space()='Submit']"));
        addEmployeeBtn.click();
    }
}
