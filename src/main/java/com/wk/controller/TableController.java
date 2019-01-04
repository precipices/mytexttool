package com.wk.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.dom4j.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.wk.entity.MyTable;
import com.wk.util.ExcelHelper;
import com.wk.util.ExeHelper;
import com.wk.util.XMLHelper;

@Controller
public class TableController {
	/**
	 * 获取根目录
	 */
	public static String rootPath() {
		// 获取根目录
		File path = null;
		try {
			path = new File(ResourceUtils.getURL("classpath:").getPath());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if (!path.exists())
			path = new File("");
		System.out.println("path:" + path.getAbsolutePath());
		return path.getAbsolutePath();
	}
	/**
	 * 根目录所在绝对路径
	 */
//	@Autowired
	private static String rootPath=rootPath();
	/**
	 * 上传文件储存路径
	 */
	public static final String UPLOAD_PATH = rootPath + "\\upload\\";
	/**
	 * 下载文件储存路径
	 */
	public static final String DOWNLOAD_PATH = rootPath + "\\download\\";
	/**
	 * bdf字体文件储存路径
	 */
	public static final String BDFFONT_PATH = rootPath+"\\font\\";
	/**
	 * 根据文件路径filePath，给出List<Map<String, Object>>格式的json数据给前端
	 */
	@ResponseBody
	@PostMapping(value = { "/importExcel.do" })
	public List<Map<String, Object>> importExcel(String uploadType,String filePath) {
		System.out.println("载入文件："+filePath);
		if("DTC".equals(uploadType)) {
			return ExcelHelper.ExcelToDTCData(filePath);
		}
		return ExcelHelper.ExcelToData(filePath);
	}

	/**
	 * 接收前端发来的Excel文件，以原文件名存放到UPLOAD_PATH中，并返回文件路径
	 */
	@PostMapping(value = "/upload.do")
	@ResponseBody
	public String upload(@RequestParam("file") MultipartFile file) {
		// 得到文件名
		String fileName = file.getOriginalFilename();
		fileName = fileName.substring(fileName.lastIndexOf('\\') + 1);
		// 设置文件路径
		String filePath = UPLOAD_PATH;
		if (!file.isEmpty()) {
			File targetFile = new File(filePath + fileName);
			try {
				// 如果父文件夹不存在则创建父文件夹
				if (targetFile.getParentFile() != null && !targetFile.getParentFile().exists()) {
					targetFile.getParentFile().mkdirs();
				}
				// 如果文件不存在则创建文件
				if (!targetFile.exists()) {
					targetFile.createNewFile();
				}
				// 将上传的文件内容复制到该文件中
				file.transferTo(targetFile);
				// FileUtils.copyInputStreamToFile(file.getInputStream(), targetFile);
			} catch (IllegalStateException | IOException e) {
				e.printStackTrace();
				return "上传失败！";
			}
		}
		System.out.println("上传文件："+filePath + fileName);
		return filePath + fileName;
	}

	/**
	 * 接收前端发来的json数据，生成普通格式xml文件，存放到DOWNLOAD_PATH中，并以List<String>结构返回所有文件的文件路径
	 */
	@PostMapping("/uploadJson2XML.do")
	@ResponseBody
	public List<String> uploadJson2XML(String tableListJson, HttpServletResponse response) {
		String filePath = DOWNLOAD_PATH;// 输出路径
		List<String> back = new ArrayList<String>();//要返回的路径列表
		//将json转化为对象
		List<MyTable> tables = JSON.parseObject(tableListJson, new TypeReference<ArrayList<MyTable>>() {});
		//循环处理每一个工作表
		for (int i = 0; i < tables.size(); i++) {
			//处理第i张表
			//创建普通格式文档对象
			Document document = XMLHelper.createXML(tables.get(i));
			//得到文件路径
			String path=filePath + tables.get(i).getTableName() + ".xml";
			//将xml文件写入路径
			XMLHelper.writeXML2Path(document,path);
			//将路径增加到列表中
			back.add(path);
		}
		System.out.println("生成文件：");
		for(int i=0;i<back.size();i++) {
			System.out.println(back.get(i));
		}
		return back;
	}

	/**
	 * 接收前端发来的json数据，生成DTC格式xml文件，存放到DOWNLOAD_PATH中，并以List<String>结构返回所有文件的文件路径
	 */
	@PostMapping("/uploadJson2DTCXML.do")
	@ResponseBody
	public List<String> uploadJson2DTCXML(String tableListJson, HttpServletResponse response) {
		String filePath = DOWNLOAD_PATH;// 输出路径
		List<String> back = new ArrayList<String>();//要返回的路径列表
		Map<String,Set<String>> fontList=new HashMap<String,Set<String>>();//字体字号列表
		//将json转化为对象
		List<MyTable> tables = JSON.parseObject(tableListJson, new TypeReference<ArrayList<MyTable>>() {});
		//循环处理每一个工作表
		for (int i = 0; i < tables.size(); i++) {
			//创建DTC格式文档对象
			Document document = XMLHelper.createDTCXML(tables.get(i));
			//得到文件路径
			String path=filePath + tables.get(i).getTableName() + ".xml";
			//将xml文件写入路径
			XMLHelper.writeXML2Path(document,path);
			//将路径增加到列表中
			back.add(path);
			//得到该表中使用到的全部字体字号
			XMLHelper.getFonts2List(tables.get(i),fontList);
		}
		//将fontList中储存的字体字号以路径形式存入到back中
		XMLHelper.fontList2String(BDFFONT_PATH,fontList,back);
		
		System.out.println("生成文件：");
		for(int i=0;i<back.size();i++) {
			System.out.println(back.get(i));
		}
		return back;
	}
	@PostMapping("/isFileExists.do")
	@ResponseBody
	public boolean isFileExists(String url) {
		File file=new File(url);
		// 如果文件不存在
		if (!file.exists()) {
			return false;
		}
		return true;
	} 
	/**
	 * 根据路径url下载文件
	 */
	@PostMapping("/downloadFile.do")
	public void download(String url, HttpServletResponse response) {
		File file = new File(url);
		response.setCharacterEncoding("utf-8");
		response.setContentType("application/octet-stream");
		OutputStream os = null;
		try {
			response.setHeader("Content-Disposition",
					"attachment; filename=" + URLEncoder.encode(file.getName(), "UTF-8"));
			os = response.getOutputStream();
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("下载文件："+url);
		boolean flag=XMLHelper.downloadFile(url,os);
		System.out.println(flag);
	}
	/**
	 * 根据路径打开BDF生成工具，注意：该工具在服务端打开，请保证前后端运行在一个机器上
	 */
	@PostMapping("/openBDFExe.do")
	@ResponseBody
	public boolean openBDFExe(@Value("${default.BDFExePath}") String relativePath , HttpServletResponse response) {
		String path=rootPath+"\\"+relativePath;
		System.out.println(path);
		return ExeHelper.openExe(path);
	}
	/**
	 * 根据路径打开BDF查看工具，注意：该工具在服务端打开，请保证前后端运行在一个机器上
	 */
	@PostMapping("/openBDFReaderExe.do")
	@ResponseBody
	public boolean openBDFReaderExe(@Value("${default.BDFReaderExePath}") String relativePath , HttpServletResponse response) {
		String path=rootPath+"\\"+relativePath;
		System.out.println(path);
		return ExeHelper.openExe(path);
	}

}
