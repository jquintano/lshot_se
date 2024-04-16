package org.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class PicUploader {
    public static void main(String[] args) throws Exception {
        FileRenamer.renameFilesByTime(Utils.ssPath);
        File directory = new File(Utils.ssPath);
        File[] filesInDirectory = directory.listFiles();
        System.setProperty(Utils.propertySetup, Utils.driverPath);
        WebDriver driver = new ChromeDriver();
        Map<String, String> fileLinkMap = new LinkedHashMap<>();

        driver.get(Utils.lightshotURL);
//        driver.manage().window().maximize();
        if (filesInDirectory != null) {
            processFiles(driver, filesInDirectory, fileLinkMap);
        }
        driver.quit();
        createExcelFile(fileLinkMap);
    }

    private static void processFiles(WebDriver driver, File[] filesInDirectory, Map<String, String> fileLinkMap) throws InterruptedException {
        for (int i = 0; i < filesInDirectory.length; i++) {
            File pictureFile = filesInDirectory[i];
            if (pictureFile.isDirectory() || !pictureFile.getName().endsWith("png")) {
                continue;
            }
            String fileName = pictureFile.getName().split("\\.")[0];
            String pictureFilePath = pictureFile.getAbsolutePath();
//            System.out.println("PATH: " + pictureFilePath);
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            System.out.print("Processing " + i + "...");

//            driver.findElement(By.xpath("/html/body/div[2]/div[1]/div[1]/form/input")).sendKeys(pictureFilePath);
            driver.findElement(By.cssSelector(Utils.inputCss)).sendKeys(pictureFilePath);

            Thread.sleep(Utils.sleepTime);
            WebElement element = driver.findElement(By.xpath(Utils.hrefXpath));
            String hrefValue = element.getAttribute("href");
            fileLinkMap.put(fileName, hrefValue);
            System.out.println(hrefValue);
            driver.navigate().refresh();
        }
    }

    private static void createExcelFile(Map<String, String> fileLinkMap) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Links");
        createHeaderRow(sheet);
        populateRows(fileLinkMap, sheet);
        sortRows(sheet);
        writeOutputToFile(workbook);
    }

    private static void createHeaderRow(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("file");
        headerRow.createCell(1).setCellValue("url");
        headerRow.createCell(2).setCellValue("sort");
        headerRow.createCell(3).setCellValue("url2");
    }

    private static void populateRows(Map<String, String> fileLinkMap, Sheet sheet) {
        int rowNum = 1;
        for (Map.Entry<String, String> entry : fileLinkMap.entrySet()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue());
            row.createCell(2).setCellValue(Integer.parseInt(entry.getKey()));
            row.createCell(3).setCellFormula("HYPERLINK(\"" + entry.getValue() + "\")");
        }
    }

    private static void sortRows(Sheet sheet) {
        Comparator<Row> rowComparator = Comparator.comparingInt(r -> (int) r.getCell(2).getNumericCellValue());
        List<Row> rows = new ArrayList<>();
        for (Iterator<Row> it = sheet.rowIterator(); it.hasNext(); ) {
            Row r = it.next();
            if (r.getRowNum() != 0) {
                rows.add(r);
            }
        }
        rows.sort(rowComparator);
        for (int i = 0; i < rows.size(); i++) {
            copyCellData(rows.get(i), sheet.getRow(i + 1));
        }
    }

    private static void copyCellData(Row sourceRow, Row targetRow) {
        for (int j = 0; j < sourceRow.getLastCellNum(); j++) {
            Cell sourceCell = sourceRow.getCell(j);
            Cell targetCell = targetRow.getCell(j);
            if (sourceCell != null && targetCell != null) {
                if (sourceCell.getCellType() == CellType.STRING) {
                    targetCell.setCellValue(sourceCell.getStringCellValue());
                } else if (sourceCell.getCellType() == CellType.NUMERIC) {
                    targetCell.setCellValue(sourceCell.getNumericCellValue());
                } else if (sourceCell.getCellType() == CellType.BOOLEAN) {
                    targetCell.setCellValue(sourceCell.getBooleanCellValue());
                }
            }
        }
    }

    private static void writeOutputToFile(Workbook workbook) throws Exception {
        FileOutputStream fileOutputStream = new FileOutputStream(Utils.outputPath);
        workbook.write(fileOutputStream);
        fileOutputStream.close();
        workbook.close();
    }
}

