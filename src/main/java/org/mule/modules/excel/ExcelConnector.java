package org.mule.modules.excel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.mule.api.annotations.Connector;
import org.mule.api.annotations.display.Summary;
import org.mule.api.annotations.MetaDataScope;
import org.mule.api.annotations.Processor;
import org.mule.api.annotations.param.Default;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

@Connector(name="excel", friendlyName="Excel")
@MetaDataScope( DataSenseResolver.class )
public class ExcelConnector {
	
	public static final int EXCEL_STYLE_ESCAPING = 0;
	
    private Workbook workbook = null;
    private List<List<String>> excelData = null;
    private int maxRowWidth = 0;
    private int formattingConvention = 0;
    private DataFormatter formatter = null;
    private FormulaEvaluator evaluator = null;
    private String separator = ",";
    
    /**
     * Custom processor
     *
     * {@sample.xml ../../../doc/excel-connector.xml.sample excel:convertexcel}
     *
     * @param fileName Excel file location.
     * @param sheetName Sheet in Excel file to retrieve data.
     * @param fileIncludesHeaderRow Headers are included in file.
     * @return Data in List<Map> format.
     */
    @Processor
    @Summary("Convert Excel to Maps")
    public List<Map<String,String>> ConvertExcel(String fileName, String sheetName, @Default("true") boolean fileIncludesHeaderRow) throws FileNotFoundException, IOException, IllegalArgumentException, InvalidFormatException{
    	
    	File source = new File(fileName);
    	File[] filesList = null;
    	
    	if(source.isDirectory()) {
            filesList = source.listFiles(new ExcelFilenameFilter());
        }
        else {
            filesList = new File[]{source};
        }
    	
    	for(File excelFile : filesList) {
            // Open the workbook
            this.openWorkbook(excelFile);
            // Convert it's contents into Map
            this.convertSheetToMap(sheetName); 
        }
    	
    	return generateKeyValues();
    }
    
    private void convertSheetToMap(String sheetName) {
        Sheet sheet = null;
        Row row = null;
        int lastRowNum = 0;
        this.excelData = new ArrayList<List<String>>(); 
        
        int numSheets = this.workbook.getNumberOfSheets();

        for(int i = 0; i < numSheets; i++) {
        	
        	if (this.workbook.getSheetName(i).equals(sheetName)){
        		sheet = this.workbook.getSheetAt(i);
                if(sheet.getPhysicalNumberOfRows() > 0) {

                    lastRowNum = sheet.getLastRowNum();
                    for(int j = 0; j <= lastRowNum; j++) {
                        row = sheet.getRow(j);
                        this.rowToArrayList(row);
                    }
                }
        	}
        }
    }
    
    private void rowToArrayList(Row row) {
        Cell cell = null;
        int lastCellNum = 0;
        ArrayList<String> excelLine = new ArrayList<String>();

        if(row != null) {
            lastCellNum = row.getLastCellNum();
            for(int i = 0; i <= lastCellNum - 1; i++) {
                cell = row.getCell(i);
                if(cell == null) {
                	excelLine.add("");
                }
                else {
                    if(cell.getCellType() != Cell.CELL_TYPE_FORMULA) {
                    	excelLine.add(this.formatter.formatCellValue(cell));
                    }
                    else {
                    	excelLine.add(this.formatter.formatCellValue(cell, this.evaluator));
                    }
                }
            }
            
            if(lastCellNum > this.maxRowWidth) {
                this.maxRowWidth = lastCellNum;
            }
        }
        this.excelData.add(excelLine);
    }
    
    private void openWorkbook(File file) throws FileNotFoundException,
	    IOException, InvalidFormatException {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			
			this.workbook = WorkbookFactory.create(fis);
			this.evaluator = this.workbook.getCreationHelper().createFormulaEvaluator();
			this.formatter = new DataFormatter(true);
		}
		finally {
			if(fis != null) {
				fis.close();
			}
		}
	}
    
    private List<Map<String,String>> generateKeyValues() {
    	
    	List<Map<String, String>> list = new ArrayList<>();
    	List<String> line = null;
    	String csvLineElement = null;
    	
    	for(int i = 0; i < this.excelData.size(); i++) {
    		line = this.excelData.get(i);
    		
    		Map<String, String> row = new HashMap<>();  
    		
    		for(int j = 0; j < this.maxRowWidth; j++) {
    			  
    			if(line.size() > j) {
    				csvLineElement = line.get(j);
    				if(csvLineElement != null) {            						
            			row.put(this.excelData.get(0).get(j), escapeEmbeddedCharacters(line.get(j)));			
    				} else {
    					row.put(this.excelData.get(0).get(j), "");   
    				}    				
    			}
    			if(line.size() <= j) {    				
        			row.put(this.excelData.get(0).get(j), "");
    			}  
    			
    		}
    		list.add(row);
    	}
    	   	
    	return list;
    }
        
    private String escapeEmbeddedCharacters(String field) {
        StringBuffer buffer = null;

        if(this.formattingConvention == ExcelConnector.EXCEL_STYLE_ESCAPING) {

            if(field.contains("\"")) {
                buffer = new StringBuffer(field.replaceAll("\"", "\\\"\\\""));
                buffer.insert(0, "\"");
                buffer.append("\"");
            }
            else {
                buffer = new StringBuffer(field);
                if((buffer.indexOf(this.separator)) > -1 ||
                         (buffer.indexOf("\n")) > -1) {
                    buffer.insert(0, "\"");
                    buffer.append("\"");
                }
            }
            return(buffer.toString().trim());
        }

        else {
            if(field.contains(this.separator)) {
                field = field.replaceAll(this.separator, ("\\\\" + this.separator));
            }
            if(field.contains("\n")) {
                field = field.replaceAll("\n", "\\\\\n");
            }
            return(field);
        }
    }
    
    
    class ExcelFilenameFilter implements FilenameFilter {
        public boolean accept(File file, String name) {
            return(name.endsWith(".xls") || name.endsWith(".xlsx"));
        }
    }

}