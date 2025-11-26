package com.example.jira.service;

import org.springframework.stereotype.Service;


import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelService1 {

	 public static class ExcelResult {
	        public String storySummary;
	        public List<String> subtasks;

	        public ExcelResult(String storySummary, List<String> subtasks) {
	            this.storySummary = storySummary;
	            this.subtasks = subtasks;
	        }
	    }

	    public ExcelResult readExcel(String filePath, String sheetName) throws Exception {
	        FileInputStream fis = new FileInputStream(new File(filePath));
	        Workbook workbook = WorkbookFactory.create(fis);

	        Sheet sheet = workbook.getSheet(sheetName);
	        if (sheet == null) {
	            throw new RuntimeException("Sheet not found: " + sheetName);
	        }

	        List<String> subtasks = new ArrayList<>();
	        String storySummary = null;

	        for (Row row : sheet) {
	            Cell cell = row.getCell(0);
	            if (cell == null) continue;

	            String value = cell.getStringCellValue().trim();
	            if (value.isEmpty()) continue;

	            if (storySummary == null) {
	                storySummary = value; // FIRST ROW = STORY NAME
	            } else {
	                subtasks.add(value); // Remaining = Subtasks
	            }
	        }

	        workbook.close();
	        fis.close();

	        return new ExcelResult(storySummary, subtasks);
	    }
}
