package com.wk.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import com.wk.entity.MyCell;
import com.wk.entity.MyTable;
public class XMLHelper {

	public static final String symbol="\n";//生成XML文件的换行符
	
	public static final String ROOT = "table";// 根节点名
	public static final String ROW = "row";// 行节点名（一级节点）
	public static final String CELL = "cell";// 单元格节点名（二级节点）

	public static final String[] DTC_COLNAMES = { "FMI", "Description", "FTB", "SPN", "Language", "Brief", "Detail" };

	/**
	 * 读取MyTable，生成DTC格式的Document对象 
	 * 该格式的行名分别是FMI Description FTB SPN Language Brief Detail 没有列名 
	 * 导出格式如下:
	 * 根节点为ECUDefinition
	 * 每个DTC节点代表languageNum行，FMI Description FTB SPN等列作为DTC属性在languageNum行里是相同的，
	 * Language Brief Detail作为子标签Translation的属性在languageNum行里是不同的
	 * 返回null表示首行格式不正确
	 */
	public static Document createDTCXML(MyTable mytable) {
		List<List<MyCell>> table = mytable.getTableData();// 得到文件各单元格内容和信息
		Document document = DocumentHelper.createDocument();// 创建文档
		Element root = document.addElement("ECUDefinition");// 根节点
//		// 得到首行,并验证其格式是否正确
//		List<MyCell> firstRow = table.get(0);
//		for (int i = 0; i < DTC_COLNAMES.length; i++) {
//			if (!DTC_COLNAMES[i].equals(firstRow.get(i).getText())) {
//				System.out.println("导入的文件格式不正确，无法导出DTC格式XML文件");
//				return null;// 格式不正确
//			}
//		}
		int languageNum=mytable.getLanguageNum();
		int DTCNum = (table.size() - 1) / languageNum;// 得到DTC节点的数量
		// 循环处理每个DTC节点
		for (int i = 0; i < DTCNum; i++) {
			List<MyCell> rowData1 = table.get(2 * i + 1);//第一行是列名，所以从第二行开始算起
			Element DTC = root.addElement("DTC");// DTC节点
			// 为DTC节点添加FMI Description FTB SPN等四个属性（rowData2中的这四个值会被忽略）
			DTC.addAttribute("FMI", Format(rowData1.get(0).getText()))
					.addAttribute("Description", Format(rowData1.get(1).getText()))
					.addAttribute("FTB", Format(rowData1.get(2).getText()))
					.addAttribute("SPN", Format(rowData1.get(3).getText()));
			//增加languageNum个Translation节点
			for(int j=0;j<languageNum;j++) {
				List<MyCell> rowData = table.get(2 * i + j+1);
				Element Translation = DTC.addElement("Translation");
				// 为Translation节点添加Language Brief Detail等三个属性
				Translation.addAttribute("Language", Format(rowData.get(4).getText()))
						.addAttribute("Brief", Format(rowData.get(5).getText()))
						.addAttribute("Detail", Format(rowData.get(6).getText()));
			}
		}
		return document;
	}

	/**
	 * 根据document，生成xml文件，写入path中
	 */
	public static boolean writeXML2Path(Document document, String path) {
		OutputStream osw = null;
		File file = new File(path);
		// 如果父文件夹不存在则创建父文件夹
		if (file.getParentFile() != null && !file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		XMLWriter out =null;
		try {
			osw = new FileOutputStream(file);
			//设置xml文档的输出格式为缩进
			OutputFormat format = OutputFormat.createPrettyPrint();
			// 设置输出文档的编码为UTF-8
			format.setEncoding("UTF-8");
			out = new XMLWriter(osw, format);
			// 输出xml文档
			out.write(document);
			System.out.println(path);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				out.close();
			} catch (IOException e1) {
				e1.printStackTrace();
				return false;
			}
			try {
				osw.close();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
	/**
	 * 根据路径path将文件传输到os输出流中
	 */
	public static boolean downloadFile(String path,OutputStream os) {
		byte[] buff = new byte[1024];
		BufferedInputStream bis = null;
		try {
			bis = new BufferedInputStream(new FileInputStream(new File(path)));
			int t = bis.read(buff);
			while (t != -1) {
				os.write(buff, 0, t);
				os.flush();
				t = bis.read(buff);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}
			}
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}
			}
		}
		return true;
	}
	/**
	 * 读取MyTable，生成Document对象
	 */
	public static Document createXML(MyTable mytable) {
		// String tablename = mytable.getTableName();// 得到表名
		List<List<MyCell>> table = mytable.getTableData();// 得到文件各单元格内容和信息
		Document document = DocumentHelper.createDocument();// 创建文档
		Element root = document.addElement(ROOT);// 根节点
		for (int i = 0; i < table.size(); i++) {
			List<MyCell> rowData = table.get(i);
			Element row = root.addElement(ROW);// 行节点
			for (int j = 0; j < rowData.size(); j++) {
				MyCell cellData = rowData.get(j);
				Element cell = row.addElement(CELL);// 单元格节点
				// 为单元格节点设置内容
				cell.setText(Format(cellData.getText()));
				// 为单元格节点设置属性
				cell.addAttribute("fontFamily", cellData.getFontFamily())
						.addAttribute("fontSize", cellData.getFontSize() + "")
						.addAttribute("width", cellData.getWidth() + "")
						.addAttribute("height", cellData.getHeight() + "");
			}
		}
		return document;
	}

	/**
	 * 将<br/>替换成\n，并将html中的一些格式换成原本样子
	 */
	private static String Format(String htmlText) {
		String result=htmlText.replace("<br/>", symbol).replace("<br>", symbol).replace("&lt;", "<").replace("&gt;", ">")
				.replace("&nbsp;", " ");
		return result;
	}
	/**
	 * 得到table中的全部字体字号，放入到fonts中
	 */
	public static void getFonts2List(MyTable myTable, Map<String, Set<String>> fonts) {
		List<List<MyCell>> table = myTable.getTableData();// 得到文件各单元格内容和信息
		for(int i=0;i<table.size();i++) {
			List<MyCell> rowData = table.get(i);
			for(int j=0;j<rowData.size();j++) {
				MyCell cell=rowData.get(j);
				//如果列表中没有该字体，则将字体放入该表中
				if(fonts.get(cell.getFontFamily())==null) {
					fonts.put(cell.getFontFamily(), new HashSet<String>());
				}
				//将字号值加入列表，因为set不重复，所以不会有重复数据
				fonts.get(cell.getFontFamily()).add(cell.getFontSize()+"");
			}
		}
	}
	/**
	 * 从bdfFontPath找到fonts中储存的bdf字体字号的存储路径，并存入到list中
	 */
	public static void fontList2String(String bdfFontPath,Map<String, Set<String>> fonts, List<String> list) {
		String path=bdfFontPath;//bdf字体文件所在路径
		Set<Entry<String, Set<String>>> entrys=fonts.entrySet();
		Iterator<Entry<String, Set<String>>> iterator=entrys.iterator();
		while(iterator.hasNext()) {
			Entry<String, Set<String>> entry=iterator.next();
			String fonttype=entry.getKey();//字体类型
			fonttype=fonttype.replace("\"", "");//如果有引号则去掉引号
			Iterator<String> iter=entry.getValue().iterator();
			while(iter.hasNext()) {
				String fontsize=iter.next();//字号
				String filename=fonttype+"_"+fontsize+".bdf";//bdf字体文件名
				list.add(path+filename);//将文件路径放入list中
			}
		}
	}
}
