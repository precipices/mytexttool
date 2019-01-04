package com.wk;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "default")
public class YmlBean {
	@Value("${server.port}")
	public String serverPort;
//	@Value("${default.fonttype}")
	public String defaultFonttype;
//	@Value("${default.fontsize}")
	public String defaultFontsize;
//	@Value("#{'${fontfamily}'}")
	public List<String> fontFamily;
	public List<String> fontsizeFamily;
	public int defaultWidth;
	public String BDFExePath;
	@Override
	public String toString() {
		return "YmlBean [serverPort=" + serverPort + ", defaultFonttype=" + defaultFonttype + ", defaultFontsize="
				+ defaultFontsize + ", fontFamily=" + fontFamily + ", fontsizeFamily=" + fontsizeFamily + "]";
	}
	public String getServerPort() {
		return serverPort;
	}
	public void setServerPort(String serverPort) {
		this.serverPort = serverPort;
	}
	public String getDefaultFonttype() {
		return defaultFonttype;
	}
	public void setDefaultFonttype(String defaultFonttype) {
		this.defaultFonttype = defaultFonttype;
	}
	public String getDefaultFontsize() {
		return defaultFontsize;
	}
	public void setDefaultFontsize(String defaultFontsize) {
		this.defaultFontsize = defaultFontsize;
	}
	public List<String> getFontFamily() {
		return fontFamily;
	}
	public void setFontFamily(List<String> fontFamily) {
		this.fontFamily = fontFamily;
	}
	public List<String> getFontsizeFamily() {
		return fontsizeFamily;
	}
	public void setFontsizeFamily(List<String> fontsizeFamily) {
		this.fontsizeFamily = fontsizeFamily;
	}
	public int getDefaultWidth() {
		return defaultWidth;
	}
	public void setDefaultWidth(int defaultWidth) {
		this.defaultWidth = defaultWidth;
	}
	public String getBDFExePath() {
		return BDFExePath;
	}
	public void setBDFExePath(String bDFExePath) {
		BDFExePath = bDFExePath;
	}

}
