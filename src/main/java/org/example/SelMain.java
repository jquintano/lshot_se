package org.example;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.WebDriver;

public class SelMain {
    public static void main(String[] args) {
        System.setProperty("webdriver.chrome.driver", "C:\\Selenium\\Chrome\\chromedriver-win64\\chromedriver.exe");
        WebDriver driver = new ChromeDriver();
        try {
        driver.get("https://www.google.com");
        System.out.println(driver.getTitle());
        driver.quit();
        } finally {
            driver.quit();
        }

        }
    }