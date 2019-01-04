package com.wk.entity;

import java.util.List;

public class MyTable {
	private String tableName;
	private int languageNum;
	private List<List<MyCell>> tableData;
	
	@Override
	public String toString() {
		return "MyTable [tableName=" + tableName + ", \n tableData:\n" + tableData + "]";
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public int getLanguageNum() {
		return languageNum;
	}
	public void setLanguageNum(int languageNum) {
		this.languageNum = languageNum;
	}
	public List<List<MyCell>> getTableData() {
		return tableData;
	}
	public void setTableData(List<List<MyCell>> tableData) {
		this.tableData = tableData;
	}
}
