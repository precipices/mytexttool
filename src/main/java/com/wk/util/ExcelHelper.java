package com.wk.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class ExcelHelper {

	/**
	 * 读取Excel并转换为List<Map<String, Object>>的数据
	 */
	public static List<Map<String, Object>> ExcelToData(String url) {
		List<Map<String, Object>> sheets = new ArrayList<Map<String, Object>>();// 工作表数据列表（最终要返回的东西）
		FileInputStream fis = null;
		Workbook book = null;
		try {
			// 创建输入流
			fis = new FileInputStream(new File(url));
			// 得到文件中的Excel工作簿
			// book=new XSSFWorkbook(fis);
			book = WorkbookFactory.create(fis);
			// 得到工作表数量
			int sheetNum = book.getNumberOfSheets();
			for (int i = 0; i < sheetNum; i++) {
				Sheet sheet = book.getSheetAt(i);// 得到其中的一个工作表
				Map<String, Object> sheetData = new HashMap<String, Object>();// 储存一个工作表的所有参数（目前仅储存了表名和表数据）
				List<ArrayList<String>> table = new ArrayList<ArrayList<String>>();// 仅储存一个工作表的各单元格String数据
				sheetData.put("tableName", sheet.getSheetName());// 将表名放入sheetData中
				Row row = null;
				Iterator<Row> rowIterator = sheet.iterator();// 得到行迭代器
				while (rowIterator.hasNext()) {// 遍历每行
					row = rowIterator.next();
					Iterator<Cell> cellIterator = row.cellIterator();// 得到单行的单元格迭代器
					ArrayList<String> tableRow = new ArrayList<String>();// 储存表格一行的数据
					while (cellIterator.hasNext()) {
						Cell cell = cellIterator.next();// 得到单单元格
						cell.setCellType(Cell.CELL_TYPE_STRING);
						String value = cell.getStringCellValue();
						value = value.replace("<", "&lt;").replace(">", "&gt;");
						tableRow.add(value);
					}
					table.add(tableRow);// 将已取出的行数据放入table中
				}
				sheetData.put("tableData", table);// 将已取出的工作表全部单元格数据放入sheetData中
				sheets.add(sheetData);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} finally {
			try {
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sheets;
	}


	/**
	 * 读取Excel并转换为DTC格式的List<Map<String, Object>>的数据
	 */
	public static List<Map<String, Object>> ExcelToDTCData(String url) {
		List<Map<String, Object>> sheets = new ArrayList<Map<String, Object>>();// 工作表数据列表（最终要返回的东西）
		FileInputStream fis = null;
		Workbook book = null;
		try {
			// 创建输入流
			fis = new FileInputStream(new File(url));
			// 得到文件中的Excel工作簿
			// book=new XSSFWorkbook(fis);
			book = WorkbookFactory.create(fis);
			// 得到工作表数量
			int sheetNum = book.getNumberOfSheets();
			for (int i = 0; i < sheetNum; i++) {
				Sheet sheet = book.getSheetAt(i);// 得到其中的一个工作表
				Map<String, Object> sheetData = sheet2DTCTableData(sheet);// 储存一个工作表的所有参数（表名和表数据）
				sheets.add(sheetData);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} finally {
			try {
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sheets;
	}
	private static Map<String, Object> sheet2DTCTableData(Sheet sheet) {
		Map<String, Object> sheetData = new HashMap<String, Object>();// 储存一个工作表的所有参数（目前仅储存了表名和表数据）
		List<List<String>> table = new ArrayList<List<String>>();// 仅储存一个工作表的各单元格String数据
		sheetData.put("tableName", sheet.getSheetName());// 将表名放入sheetData中
		//遍历每行，存储成DTC格式
		Iterator<Row> rowIterator = sheet.iterator();// 得到行迭代器
		Row firstRow=rowIterator.next();//得到首行
		//FMI	Description	FTB	SPN
		//要生成的表格的前四列列名
		List<String> fourColName=new ArrayList<String>();
		fourColName.add("FMI");
		fourColName.add("Description");
		fourColName.add("FTB");
		fourColName.add("SPN");
		//SPN	FMI	Description	FTB
		//得到原Excel前四列列名
		List<String> fourExcelColName=new ArrayList<String>();
		for(int j=0;j<fourColName.size();j++) {
			Cell firstRowCell=firstRow.getCell(j);
			firstRowCell.setCellType(Cell.CELL_TYPE_STRING);
			String v=firstRowCell.getStringCellValue();
			fourExcelColName.add(v);
		}
		//得到所有语言种类
		firstRow.getCell(4).setCellType(Cell.CELL_TYPE_STRING);
		String begin=firstRow.getCell(4).getStringCellValue();//第一个语言名
		List<String> languages=new ArrayList<String>();//储存所有语言种类
		languages.add(begin);
		for(int j=5;j<firstRow.getLastCellNum();j++) {
			Cell firstRowCell=firstRow.getCell(j);
			firstRowCell.setCellType(Cell.CELL_TYPE_STRING);
			String v=firstRowCell.getStringCellValue();
			if(begin.equals(v)) {
				break;
			}
			languages.add(v);
		}
		sheetData.put("languageNum", languages.size());// 表示每个DTC单元有多少行
		//将首行放入返回数据中
		List<String> firstTableRow = new ArrayList<String>(fourColName);// 储存表格首行的数据
		//FMI	Description	FTB	SPN	Language	Brief	Detail
		firstTableRow.add("Language");
		firstTableRow.add("Brief");
		firstTableRow.add("Detail");
		table.add(firstTableRow);// 将已取出的首行数据放入table中
		// 遍历每行，并储存一个DTC单元到table中
		Row row = null;
		while (rowIterator.hasNext()) {
			row = rowIterator.next();//得到一行
			Iterator<Cell> cellIterator = row.cellIterator();// 得到单行的单元格迭代器
			//得到前四个固定属性的值,放入fourValue列表中
			List<String> fourValue=new ArrayList<String>();
			Map<String,String> fourValueMap=new HashMap<String,String>();
			for(int j=0;j<fourExcelColName.size();j++) {//先得到对应关系
				Cell cell = cellIterator.next();// 得到单单元格
				cell.setCellType(Cell.CELL_TYPE_STRING);
				String value = cell.getStringCellValue();
				value = value.replace("<", "&lt;").replace(">", "&gt;");
				fourValueMap.put(fourExcelColName.get(j), value);
			}
			for(int j=0;j<fourColName.size();j++) {//再存值
				String fourValueKey=fourColName.get(j);
				fourValue.add(fourValueMap.get(fourValueKey));
			}
			//增加languageNum行数据，放入DTC中
			List<List<String>> DTC = new ArrayList<List<String>>();// 储存一个DTC结构
			for(int j=0;j<languages.size();j++) {//先得到前5列
				List<String> DTCRow = new ArrayList<String>(fourValue);//FMI	Description	FTB	SPN属性
				DTCRow.add(languages.get(j));//Language属性
				DTC.add(DTCRow);
			}
			for (int k = 0; k < 2; k++) {// 再加Brief和Detail属性
				for (int j = 0; j < languages.size(); j++) {
					Cell cell = cellIterator.next();// 得到单单元格
					cell.setCellType(Cell.CELL_TYPE_STRING);
					String value = cell.getStringCellValue();
					DTC.get(j).add(value);
				}
			}
			//最后将DTC结构放入table中
			for(int j=0;j<languages.size();j++) {
				table.add(DTC.get(j));
			}
		}
		sheetData.put("tableData", table);// 将已取出的工作表全部单元格数据放入sheetData中
		return sheetData;
	}
}
