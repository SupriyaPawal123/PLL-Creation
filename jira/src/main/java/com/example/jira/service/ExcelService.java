package com.example.jira.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelService {

    public List<String> readTasks(String storyType, MultipartFile file) {

        List<String> tasks = new ArrayList<>();

        try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream())) {

            String sheetName = storyType.equalsIgnoreCase("FullPLL")
                    ? "Full PLL"
                    : "Light PLL - ";

            XSSFSheet sheet = workbook.getSheet(sheetName);

            if (sheet == null)
                throw new RuntimeException("Excel sheet not found: " + sheetName);

            Row header = sheet.getRow(0);
            int nameCol = -1;

            for (Cell cell : header) {
                if (cell.getStringCellValue().trim().equalsIgnoreCase("Name")) {
                    nameCol = cell.getColumnIndex();
                    break;
                }
            }

            if (nameCol == -1)
                throw new RuntimeException("Column 'Name' not found in sheet: " + sheetName);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);

                if (row != null) {
                    Cell cell = row.getCell(nameCol);
                    if (cell != null && cell.getCellType() == CellType.STRING) {
                        tasks.add(cell.getStringCellValue().trim());
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error reading Excel: " + e.getMessage(), e);
        }

        return tasks;
    }
}
