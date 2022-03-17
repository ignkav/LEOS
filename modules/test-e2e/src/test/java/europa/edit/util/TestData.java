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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
/*import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;*/
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestData {

    private String dataFilePath = "";

    public void readExcelPath() {
        File file = new File("");
        dataFilePath = file.getAbsolutePath() + File.separator + Constants.DATATABLE_LOCATION + File.separator + "TestData.xlsx";
    }

    public void readExcelWorkBook() {
        readExcelPath();
        File file = new File(dataFilePath);
        try {
            TestParameters.getInstance().setTestDataFile(new XSSFWorkbook(new FileInputStream(file)));
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new DecisionTestExceptions("The config file is not found on the defined path");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new DecisionTestExceptions("The config file format is not as expected");
        }
    }

/*    private int getRowNum(XSSFSheet testdataSheet, String actType) {
        int rowCount = testdataSheet.getLastRowNum() - testdataSheet.getFirstRowNum();
        int rowNum = 0;
        for (int i = 0; i < rowCount + 1; i++) {
            Row row = testdataSheet.getRow(i);
            if (row.getCell(0).getStringCellValue().contains(actType)) {
                rowNum = i;
                break;
            }
        }
        return rowNum;
    }

    private int getColNum(XSSFSheet testdataSheet, String colName) {
        int rowCount = testdataSheet.getLastRowNum() - testdataSheet.getFirstRowNum();
        int rowNum = 0;
        colloop:
        for (int i = 0; i < rowCount + 1; i++) {
            Row row = testdataSheet.getRow(i);
            for (int j = 0; j < row.getLastCellNum(); j++) {
                if (row.getCell(j).getStringCellValue().contains(colName)) {
                    rowNum = j;
                    break colloop;
                }
            }
        }
        return rowNum;
    }

    public List<String> getColums(String testdataSheetName, String colName) {
        XSSFSheet testdataSheet = TestParameters.getInstance().getTestDataFile().getSheet(testdataSheetName);
        List coulmns = new ArrayList();
        int colNumber = getColNum(testdataSheet, colName);
        for (int j = 1; j <= testdataSheet.getLastRowNum(); j++) {
            coulmns.add(testdataSheet.getRow(j).getCell(colNumber).getStringCellValue());
        }
        return coulmns;
    }

   public List<String> getTitleRows(String testdataSheetName) {
        XSSFSheet testdataSheet = TestParameters.getInstance().getTestDataFile().getSheet(testdataSheetName);
        List titleRows = new ArrayList();
        for (int j = 1; j < testdataSheet.getRow(0).getLastCellNum(); j++) {
            titleRows.add(testdataSheet.getRow(0).getCell(j).getStringCellValue());
        }
        return titleRows;
    }

   private String getCellValue(XSSFSheet worksheet, int rowNum, int columnNum) {
        try {
            XSSFRow row = worksheet.getRow(rowNum);
            XSSFCell cell = row.getCell(columnNum);
            return cell.getStringCellValue();
        } catch (NullPointerException e) {
            return "";
        }
    }

   public String getData(String testName, String colName, String sheetName) {
        XSSFSheet testdataSheet = TestParameters.getInstance().getTestDataFile().getSheet(sheetName);
        int rowNum;
        int colNum;
        colNum = getColNum(testdataSheet, colName);
        rowNum = getRowNum(testdataSheet, testName);
        return getCellValue(testdataSheet, rowNum, colNum);
    }*/
}