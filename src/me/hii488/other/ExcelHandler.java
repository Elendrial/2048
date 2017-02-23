package me.hii488.other;

import static bad.robot.excel.sheet.Coordinate.coordinate;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import bad.robot.excel.column.ExcelColumnIndex;
import bad.robot.excel.workbook.PoiWorkbook;
import me.hii488.game.Controller;


public class ExcelHandler {
	
	public static XSSFWorkbook wb;
	public static PoiWorkbook pwb;
	public static XSSFSheet[] sheets;
	public static String fileName;
	
	public static void setup(String fileName){
		try{
			ExcelHandler.fileName = fileName;
			wb = read(fileName);
			pwb = new PoiWorkbook(wb);
			
			Row row = wb.getSheetAt(0).getRow(0);
			
			int i = 0;
			boolean found = false;
			Cell c;
			do{
				i++;
				c = row.getCell(i);
				if(c!=null){
					System.out.println("value of " + i + ": " + c.getStringCellValue());
				
					if(c.getStringCellValue().equals("")){
						found = true;
					}
				}
				else{
					found = true;
				}
				
			}while(!found);
			Controller.run = i;
		}
		catch(Exception e){e.printStackTrace();}
	}
	
	public static void writeGeneration(int generation, String average, int run){
		pwb.replaceCell(coordinate(ExcelColumnIndex.valueOf(GeneralHelper.toAlphabetic((run-1)*2+1)),generation+1), Double.parseDouble(average.split(":")[1].trim()));
		finalWrite();
	}
	
	public static void writeGenInfo(int run, String info){
		String[] s = info.split("\n");
		for(int i = 0; i < s.length; i++)
			pwb.replaceCell(coordinate(ExcelColumnIndex.valueOf(GeneralHelper.toAlphabetic((run-1)*2+2)),i+1), info);
		finalWrite();
	}
	
	public static void finalWrite(){
		FileOutputStream out;
		try {
			out = new FileOutputStream(fileName);
			
	        wb.write(out);
	        out.flush();
	        out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static XSSFWorkbook read(String file) throws IOException {
		InputStream excelFile = new FileInputStream(file); 
        return new XSSFWorkbook(excelFile);
    }
	
}
