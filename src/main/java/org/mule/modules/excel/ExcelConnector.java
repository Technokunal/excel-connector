package org.mule.modules.excel;

import java.util.Arrays;
import java.util.Map;
import java.util.List;

import org.mule.api.annotations.Config;
import org.mule.api.annotations.Connector;
import org.mule.api.annotations.param.MetaDataKeyParam;
import org.mule.api.annotations.MetaDataScope;
import org.mule.api.annotations.Processor;
import org.mule.api.annotations.param.Default;
import org.mule.modules.excel.config.ConnectorConfig;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

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
    private List<List<String>> csvData = null;
    private int maxRowWidth = 0;
    private int formattingConvention = 0;
    private DataFormatter formatter = null;
    private FormulaEvaluator evaluator = null;
    private String separator = ",";

    @Config
    ConnectorConfig config;
	
    /**
     * Custom processor
     *
     * {@sample.xml ../../../doc/excel-connector.xml.sample excel:convertexcel}
     *
     * @param fileName Excel file location.
     * @param sheetName Sheet in Excel file to retrieve data.
     * @return Data in CSV format.
     */
    @Processor
    public List<List<String>> ConvertExcel(String fileName, String sheetName) throws FileNotFoundException, IOException, IllegalArgumentException, InvalidFormatException{
    	
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
            // Convert it's contents into a CSV file
            this.convertSheetToCSV(sheetName); 
        }
    	
    	//return saveCSVFile();
    	return this.csvData;
    }
        
    public ConnectorConfig getConfig() {
        return config;
    }

    public void setConfig(ConnectorConfig config) {
        this.config = config;
    }
    
    private void convertSheetToCSV(String sheetName) {
        Sheet sheet = null;
        Row row = null;
        int lastRowNum = 0;
        this.csvData = new ArrayList<List<String>>();        

        System.out.println("Converting files contents to CSV format.");

        // Discover how many sheets there are in the workbook....
        int numSheets = this.workbook.getNumberOfSheets();

        // and then iterate through them.
        for(int i = 0; i < numSheets; i++) {
        	
        	if (this.workbook.getSheetName(i).equals(sheetName)){
        		sheet = this.workbook.getSheetAt(i);
                if(sheet.getPhysicalNumberOfRows() > 0) {

                    lastRowNum = sheet.getLastRowNum();
                    for(int j = 0; j <= lastRowNum; j++) {
                        row = sheet.getRow(j);
                        this.rowToCSV(row);
                    }
                }
        	}
        }
    }
    
    private void rowToCSV(Row row) {
        Cell cell = null;
        int lastCellNum = 0;
        ArrayList<String> csvLine = new ArrayList<String>();

        if(row != null) {
            lastCellNum = row.getLastCellNum();
            for(int i = 0; i <= lastCellNum - 1; i++) {
                cell = row.getCell(i);
                if(cell == null) {
                    csvLine.add("");
                }
                else {
                    if(cell.getCellType() != Cell.CELL_TYPE_FORMULA) {
                        csvLine.add(this.formatter.formatCellValue(cell));
                    }
                    else {
                        csvLine.add(this.formatter.formatCellValue(cell, this.evaluator));
                    }
                }
            }
            
            if(lastCellNum > this.maxRowWidth) {
                this.maxRowWidth = lastCellNum - 1;
            }
        }
        this.csvData.add(csvLine);
    }
    
    private void openWorkbook(File file) throws FileNotFoundException,
	    IOException, InvalidFormatException {
		FileInputStream fis = null;
		try {
			System.out.println("Opening workbook [" + file.getName() + "]");
			
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
    
    
    private String saveCSVFile() {
    	
    	List<String> line = null;
    	String csvLineElement = null;
    	StringBuilder sb = new StringBuilder();
    	
    	System.out.println("Converting to CSV");
    	
    	for(int i = 0; i < this.csvData.size(); i++) {
    		line = this.csvData.get(i);
            for(int j = 0; j < this.maxRowWidth; j++) {
            	if(line.size() > j) {
                    csvLineElement = line.get(j);
                    if(csvLineElement != null) {
                        //sb.append(this.escapeEmbeddedCharacters(csvLineElement));
                    	sb.append(csvLineElement);
                    }
                }
                if(j < (this.maxRowWidth - 1)) {
                    sb.append(this.separator);
                }
            }
            
            if(i < (this.csvData.size() - 1)) {
                sb.append("\n");
            }
            
    	}
    	return sb.toString();
    }
    
    /*
    private void saveCSVFile() throws FileNotFoundException, IOException {
    	
	    FileWriter fw = null;
	    BufferedWriter bw = null;
	    ArrayList<String> line = null;
	    StringBuffer buffer = null;
	    String csvLineElement = null;
	    try {
	
	        // Open a writer onto the CSV file.
	        fw = new FileWriter(file);
	        bw = new BufferedWriter(fw);
	
	        // Step through the elements of the ArrayList that was used to hold
	        // all of the data recovered from the Excel workbooks' sheets, rows
	        // and cells.
	        for(int i = 0; i < this.csvData.size(); i++) {
	            buffer = new StringBuffer();
	
	            line = this.csvData.get(i);
	            for(int j = 0; j < this.maxRowWidth; j++) {
	                if(line.size() > j) {
	                    csvLineElement = line.get(j);
	                    if(csvLineElement != null) {
	                        buffer.append(this.escapeEmbeddedCharacters(csvLineElement));
	                    }
	                }
	                if(j < (this.maxRowWidth - 1)) {
	                    buffer.append(this.separator);
	                }
	            }
	
	            // Once the line is built, write it away to the CSV file.
	            bw.write(buffer.toString().trim());
	
	            // Condition the inclusion of new line characters so as to
	            // avoid an additional, superfluous, new line at the end of
	            // the file.
	            if(i < (this.csvData.size() - 1)) {
	                bw.newLine();
	            }
	        }
	    }
	    finally {
	        if(bw != null) {
	            bw.flush();
	            bw.close();
	        }
	    }
	}
    */
    
    private String escapeEmbeddedCharacters(String field) {
        StringBuffer buffer = null;

        // If the fields contents should be formatted to confrom with Excel's
        // convention....
        if(this.formattingConvention == ExcelConnector.EXCEL_STYLE_ESCAPING) {

            // Firstly, check if there are any speech marks (") in the field;
            // each occurrence must be escaped with another set of speech marks
            // and then the entire field should be enclosed within another
            // set of speech marks. Thus, "Yes" he said would become
            // """Yes"" he said"
            if(field.contains("\"")) {
                buffer = new StringBuffer(field.replaceAll("\"", "\\\"\\\""));
                buffer.insert(0, "\"");
                buffer.append("\"");
            }
            else {
                // If the field contains either embedded separator or EOL
                // characters, then escape the whole field by surrounding it
                // with speech marks.
                buffer = new StringBuffer(field);
                if((buffer.indexOf(this.separator)) > -1 ||
                         (buffer.indexOf("\n")) > -1) {
                    buffer.insert(0, "\"");
                    buffer.append("\"");
                }
            }
            return(buffer.toString().trim());
        }
        // The only other formatting convention this class obeys is the UNIX one
        // where any occurrence of the field separator or EOL character will
        // be escaped by preceding it with a backslash.
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