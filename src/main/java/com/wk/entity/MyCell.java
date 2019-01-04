package com.wk.entity;

public class MyCell {
	private String text;
	private String fontFamily;
	private int fontSize;
	private int height;
	private int width;
	@Override
	public String toString() {
		return "MyCell [\ntext=" + text + ",\n fontFamily=" + fontFamily + ",\n fontSize=" + fontSize + ",\n height=" + height
				+ ",\n width=" + width + "]\n";
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getFontFamily() {
		return fontFamily;
	}
	public void setFontFamily(String fontFamily) {
		this.fontFamily = fontFamily;
	}
	public int getFontSize() {
		return fontSize;
	}
	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
}