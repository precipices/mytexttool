package com.wk.util;

public class ExeHelper {
	// 调用其他的可执行文件，例如：自己制作的exe，或是 下载 安装的软件.
	public static boolean openExe(String path) {
		Runtime rn = Runtime.getRuntime();
		Process p = null;
		try {
			p = rn.exec(path);
		} catch (Exception e) {
			System.out.println("Error exec:"+path);
			return false;
		}
		return true;
	}
}
