/**
 * 
 */
// 全局变量
var tableList = [];// Table类的列表，每一个代表一个sheet
var showTable = 0;// 当前显示的table的编号
var tableNum = 0;// table的数量
var mytable = null;// 存放当前显示的表的Table类
var filePath = "";// 服务器上的文件路径
var uploadType="nomal";
//载入普通格式Excel
function uploadExcel(){
	uploadType="nomal";
	// 打开模态框
	$("#myModal").modal("show");
}
function uploadDTCExcel(){
	uploadType="DTC";
	// 打开模态框
	$("#myModal").modal("show");
}

//载入文件
var importExcel = function(filePath) {
	var url="/importExcel.do";
	$.ajax({
		type : "POST",
		url : url,
		data : {
			"uploadType" : uploadType,
			"filePath" : filePath
		},
		success : function(data) {
			// 清空div #mytable中的内容
			$("#mytable").html("");
			tableNum = data.length;
			var workSheetHtml = "";
			// 初始化所有工作表
			for (var i = 0; i < tableNum; i++) {
				var obj = new Table();// 创建新Table对象
				obj.No = i;// 赋予表格编号
				obj.init(data[i]);// 初始化表格
				tableList[i] = obj;// 将表格放入表格列表中
				workSheetHtml += "<option value=" + i + ">" + obj.tableName
						+ "</option>"
			}
			// 初始化workSheet下拉框
			$("#workSheet").html(workSheetHtml);
			// 显示第一个工作表
			mytable = tableList[0];
			mytable.mytable.show();
			alert("载入成功！");
		}
	});
}

// 导出当前表的xml文件
var downloadAXml = function() {
	downloadXML([ Util.table2Json(mytable)]);
}
// 导出全部表的XML文件
var downloadAllXml = function() {
	var tableListJson = [];
	for (var i = 0; i < tableList.length; i++) {
		tableListJson[i] = Util.table2Json(tableList[i]);
	}
	downloadXML(tableListJson);
}
// 当前工作表导出DTC格式xml
var downloadADTCXml = function() {
	if(!checkDTCTable()){
		alert("表格不符合DTC格式！请注意导入的格式！");
		return false;
	}
	downloadDTCXML([ Util.table2Json(mytable)]);
}
// 全部工作表导出DTC格式xml
var downloadAllDTCXml = function() {
	if(!checkDTCTable()){
		alert("表格不符合DTC格式！请注意导入的格式！");
		return false;
	}
	var tableListJson = [];
	for (var i = 0; i < tableList.length; i++) {
		tableListJson[i] = Util.table2Json(tableList[i]);
	}
	downloadDTCXML(tableListJson);
}
//验证第n张表是否DTC格式
function checkDTCTable(){
	if(uploadType=="DTC")
		return true;
	return false;
//	//DTC格式首行名
//	var DTC_COLNAMES = [ "FMI", "Description", "FTB", "SPN", "Language", "Brief", "Detail" ];
//	//得到首行
//	var firstRow=tableList[n].tableData[0];
//	for(var i=0;i<DTC_COLNAMES.length;i++){
//		if(!DTC_COLNAMES[i]==firstRow[i])
//			return false;
//	}
//	return true;
}
// 验证上传类型
function validateType(ele) {
	// 返回 KB，保留小数点后两位
	// alert((ele.files[0].size/(1024*1024)).toFixed(2));
	var file = ele.value;
	if (!/.(xls|xlsx)$/.test(file)) {
		alert("上传类型必须是xls,xlsx中的一种");
		$(ele).val("");
		return false;
	}
	// 返回Byte(B),保留小数点后两位
	if (((ele.files[0].size).toFixed(2)) >= (20 * 1024 * 1024)) {
		alert("请上传小于20M的文件");
		$(ele).val("");
		return false;
	}
}
// 将json表格数据上传并生成下载链接
function downloadXML(tableListJson) {
	$.ajax({
		type : "POST",
		url : "/uploadJson2XML.do",
		data : {
			"tableListJson" : JSON.stringify(tableListJson)
		},
		success : function(data) {
			$("#downloadDiv").html("");
			for (var i = 0; i < data.length; i++) {
				addFrom2DownloadDiv(data[i]);
			}
			alert("导出成功！");
		}
	});
}
// 将json表格数据上传并生成下载链接
function downloadDTCXML(tableListJson) {
	$.ajax({
		type : "POST",
		url : "/uploadJson2DTCXML.do",
		data : {
			"tableListJson" : JSON.stringify(tableListJson)
		},
		success : function(data) {
			$("#downloadDiv").html("");
			for (var i = 0; i < data.length; i++) {
				addFrom2DownloadDiv(data[i]);
			}
			alert("导出成功！");
		}
	});
}
//添加一个新的下载表单到downloadDiv框，指向url路径
function addFrom2DownloadDiv(url) {
	var sensor = "<form action='/downloadFile.do' method='post'><label>"
			+ url.substring(url.lastIndexOf('\\') + 1)
			+ "</label><input type='hidden' name='url' value='"
			+ url
			+ "'/><input type='button' class='btn downloadButton' value='下载'/></form>";
	$("#downloadDiv").append(sensor + "<br/>");
}
//判断服务器上url路径的文件是否存在
function isFileExists(url){
	var result=false;
	$.ajax({
		type:"POST",
		url:"/isFileExists.do",
		data:{
			"url":url
		},
		async:false,
		success:function(data){
			result= data;
		}
	});
	return result;
}
// Table类
var Table = function() {
	this.No = -1;
	this.filePath = "/";
	this.tableName = "未命名";
	this.languageNum=2;
	this.tableData = [];
	this.mytable = null;
	this.rowNum = 0;
	this.colNum = 0;
	this.selectOne = null;
	this.selectRow = -1;
	this.selectCol = -1;
	// 初始化全部列宽度
	this.initRowWidth = function(width) {
		// 表格宽度为0
		this.mytable.width(0);
		// //改变每列的宽度，并加到表格宽度上
		// for (var i = 0; i < this.colNum; i++) {
		// var cell = this.getCell(0, i);
		// var newTableWidth = parseInt(this.mytable.width()) + parseInt(width)-
		// parseInt(cell.width());
		// this.mytable.width(newTableWidth);
		// cell.width(width);
		// }
		// 对每列进行换行
		for (var i = 0; i < this.colNum; i++) {
			this.changeWidth(i, width);
		}
	};
	// 初始化mytable对象
	this.init = function(data) {
		// 将表格数据加载到网页中"<div id='table"+this.No"'>"+
		var tableHtml = "<table id='table" + this.No + "'><caption>"//' class='table table-striped'
				+ data.tableName + "</caption>";
		for (var i = 0; i < data.tableData.length; i++) {
			var tableRow = data.tableData[i];
			tableHtml += "<tr>";
			for (var j = 0; j < tableRow.length; j++) {
				// tableHtml += "<td><textarea class='form-control'
				// style='overflow-x= hidden; overflow-y= hidden;'>" +
				// tableRow[j] + "</textarea></td>";
				tableHtml += "<td>" + tableRow[j] + "</td>";
			}
			tableHtml += "</tr>";
		}
		tableHtml += "</table>";
		$("#mytable").append(tableHtml);
		// 初始化一些表格参数
		this.tableName = data.tableName;
		this.languageNum=data.languageNum;
		this.rowNum = data.tableData.length;
		this.colNum = data.tableData[0].length;
		// 将表格放入对象中
		this.tableData = data.tableData;
		this.mytable = $("#table" + this.No);
		// 初始化列宽度
		this.initRowWidth(defaultWidth);
		// 隐藏表格
		this.mytable.hide();
	};
	// 根据行列号选择表格中的某个单元格
	this.getCell = function(row, col) {
		return this.mytable.find("tr:eq(" + row + ") td:eq(" + col + ")");
	};
	// 根据行列号选择表格中的某个单元格原初数据
	this.getData = function(row, col) {
		return this.tableData[row][col];
	};
	// 得到指定行的所有单元格
	this.getRowCells = function(row) {
		if (this.mytable.find("tr:eq(" + row + ") td").length <= 0)
			return [];
		var cells = [];
		for (var i = 0; i < this.colNum; i++) {
			cells[i] = this.getCell(row, i);
		}
		return cells;
		// return this.mytable.find("tr:eq(" + row + ") td");
	};
	// 得到表格的所有单元格
	this.getAllCells = function() {
		var cells = [];
		var p = 0;
		for (var i = 0; i < this.rowNum; i++) {
			var rowCells = this.getRowCells(i);
			for (var j = 0; j < rowCells.length; j++) {
				cells[p++] = rowCells[j];
			}
		}
		return cells;
	}
	// 得到指定行的所有表格数据
	this.getRowDatas = function(row) {
		return this.tableData[row];
	};
	// 得到指定列的所有单元格
	this.getColCells = function(col) {
		var cells = [];
		for (var i = 0; i < this.rowNum; i++) {
			cells[i] = this.getCell(i, col);
		}
		return cells;
	};
	// 得到指定行的所有表格数据
	this.getColDatas = function(col) {
		var datas = [];
		for (var i = 0; i < this.rowNum; i++) {
			datas[i] = this.tableData[i][col];
		}
		return datas;
	};
	// 获取文本宽度
	this.textWidth = function(row, col) {
		var text = this.getCell(row, col).text();
		var sensor = $('<pre>' + text + '</pre>').css({
			display : 'none'
		});
		$('body').append(sensor);
		var width = sensor.width();
		sensor.remove();
		return width;
	};
	// 获取文本高度
	this.textHeight = function(row, col) {
		var text = this.getCell(row, col).text();
		var sensor = $('<pre>' + text + '</pre>').css({
			display : 'none'
		});
		$('body').append(sensor);
		var height = sensor.height();
		sensor.remove();
		return height;
	};
	// 改变行高度,最小高度为20
	this.changeHeight = function(row, height) {
		if (height < 20)
			height = 20;
		var cell = this.getCell(row, 0);
		cell.height(height);
	};
	// 改变列宽度,最小宽度为20
	this.changeWidth = function(col, width) {
		if (width < 20)
			width = 20;
		var cell = this.getCell(0, col);
		// var oldWidth = cell.width();
		// var oldTableWidth = this.mytable.width();
		var newTableWidth = parseInt(this.mytable.width()) + parseInt(width)
				- parseInt(cell.width());
		this.mytable.width(newTableWidth);
		cell.width(width);
		// 处理单元格数据
		var colDatas = this.getColDatas(col);
		var colCells = this.getColCells(col);
		for (var i = 0; i < this.rowNum; i++) {
			Util.lineFeeds(this, i, col);
		}
	};
	//单单元格根据列宽重新换行
	this.reWidth=function(row,col){
		Util.lineFeeds(this, row, col);
	};
	//全表根据列宽重新换行
	this.reWidth=function(){
		for(var i=0;i<this.rowNum;i++){
			for(var j=0;j<this.colNum;j++){
				Util.lineFeeds(this, i, j);
			}
		}
	}
}