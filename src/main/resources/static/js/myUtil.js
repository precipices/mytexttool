/**
 * 
 */
var Util = {
	// 换行符
	symbol:"<br/>",
	// 获取文本高度
	getHeight : function(text,template) {
		var sensor = $('<span>' + text + '</span>').css({
			display : 'none',
			"font-size":template.css("font-size")
		});
		$('body').append(sensor);
		var height = sensor.height();
		sensor.remove();
		return height;
	},
	// 获取文本宽度
	getWidth : function(text,template) {
		var sensor = $('<span>' + text + '</span>').css({
			display : 'none',
			"font-size":template.css("font-size")
		});
		$('body').append(sensor);
		var width = sensor.width();
		sensor.remove();
		return width;
	},
	// 根据路径下载服务器上的文件
	downloadFile : function(url){
		var sensor = $("<form action='/downloadFile.do' method='post'><input type='hidden' name='url' value='"
				+ url + "'/></form>").css({
					display : 'none'
				});
		$('body').append(sensor);
		sensor.submit();
		sensor.remove();
	},

	// 将表格转化为json字符串
	table2Json:function(table){
		var tableJson = new Object();// 一张工作表的全部信息
		tableJson.tableName = table.tableName;
		tableJson.languageNum=table.languageNum;
		var rowArrayJson = [];// 一张工作表的所有单元格的全部信息
		for (var j = 0; j < table.rowNum; j++) {
			var rowCells = table.getRowCells(j);
			var rowJson = [];// 工作表的一行单元格的全部信息
			for (var k = 0; k < rowCells.length; k++) {
				var cell = rowCells[k];
				var cellJson = new Object();
				if (cell.html())
					cellJson.text = cell.html();
				else
					cellJson.text = "";
				cellJson.fontFamily = cell.css("font-family");
				cellJson.fontSize = parseInt(cell.css("font-size"));
				cellJson.height = cell.height();
				cellJson.width = cell.width();
				rowJson[k] = cellJson;
			}
			rowArrayJson[j] = rowJson;
		}
		tableJson.tableData = rowArrayJson;
		return tableJson;
	},
	// 判断字符是否是标点符号（如果是字符串则判断是否包含标点符号）
	isPuntuation:function(ch){
	    var reg = /["',，.。/、\]\[【】！!?？——_<>%;‘’；)《（）》(&+=`“”·*#@@]/;
		return reg.test(ch);
	},
	// 判断字符是否是英文字母（如果是字符串则判断是否全为英文字母）
	isLetter:function(ch){
		var reg=/^[a-zA-Z]+$/;
		return reg.test(ch);
	},
	// 判断字符是否是英文字母，包括连接符（如果是字符串则判断是否全为英文字母）
	isWord:function(ch){
		var reg=/^[a-zA-Z-]+$/;
		return reg.test(ch);
	},
	// 向字符串old指定下标point处插入字符(或字符串)ch,返回新字符串
	insertChar:function(old,point,ch){
		var head=old.substring(0,point);
		var rear=old.substring(point,old.length);
		return head+ch+rear;
	},
	// 得到字符串中某个下标所在的单词及单词首末位所在下标,如果包含连接符“-”，则返回其位置否则返回-1
	getWordOfChar:function(str,p){
		var head=p;
		var rear=p;
		// head前推到不是英文字母和下划线为止
		while(head>0&&this.isWord(str.charAt(head-1)))head--;
		// rear后推到不是英文字母和下划线为止
		while((rear<str.length-1) && this.isWord(str.charAt(rear+1)))rear++;
		var word=str.substring(head,rear+1);
		var position=word.indexOf("-");
		if(position>0)
			position+=head;
		return {"head":head,"rear":rear,"word":word,"symPosition":position};
	},
	// 判断英文单词是否复合词组（及单词是否包含-）
	isCompound:function(word){
		var reg=/[-]/;
		return reg.test(word);
	},
	// 得到字节长度
	getByteLen:function(str) {
		if(!str) return 0;// 如果str为空，返回0
		var len = 0;
		for (var i = 0; i < str.length; i++) {
			var a = str.charAt(i);
			if (a.match(/[^\x00-\xff]/ig) != null){
				len += 2;
			}else{
				len += 1;
			}
		}
		return len;
	},
	// 得到指定字节所在字符位置
	getPointByByte:function(str,position){
		var p=0;
		var len=0;
		for(p=0;p<str.length&&len<position;p++){
			var a=str.charAt(p);
			if(a.match(/[^\x00-\xff]/ig) != null){
				len+=2;
			}else{
				len+=1;
			}
		}
		return p;
	},
	// 根据像素长度切割一次字符串
	cutOneLine : function(str, width,template) {
		// 如果str为空
		if(!str) return {"before":"","after":""};
		/**
		 * 1.当控件宽度不足以显示下一个字符时将该字符完整显示在下一行
		 */
		var pxWidth=this.getWidth(str,template);// 字符串str的像素长度
		var chWidth=this.getByteLen(str);// 字符串str的字节长度（即字节数量）
		var p=this.getPointByByte(str,parseInt(chWidth*width/pxWidth));// 切割点下标（第二行第一个字符在原字符串中的下标）
		// 会因为浏览器兼容性和编码导致计算失误，所以根据长度再检测一遍,确保不超过边框
		while(this.getWidth(str.substring(0,p),template)>=width)
			p--;
		/**
		 * 2.当控件宽度不足以显示下一个符号时必须将符号前的一个非符号字符一起换行
		 */
		// 判断切割点是否为符号,是则将切割点前推一位
		while(this.isPuntuation(str.charAt(p))){
			p--;
		}
		// 判断切割点及前一个字符是否英文
		if(this.isLetter(str.charAt(p-1)) && this.isLetter(str.charAt(p))){
			// 得到切割点位置的单词及首末下标
			var word=this.getWordOfChar(str,p);
			/**
			 * 3.英文复合词组换行时，必须在复合词连接符处换行，如果控件不足以显示前半个单词，则将整个复合词组换行显示
			 */
			// 判断切割点所在单词是否是复合词组
			if(word.symPosition>=0){
				// 如果是复合词组
				// 足以显示前半个单词，将切割点移动到连接符后
				if(word.symPosition<p)
					p=word.symPosition+1;
				// 不足以显示前半个单词，且头部下标不为0，将切割点移动到单词头部
				else if(word.head>0){
					p=word.head;
				}
			}else{
				/**
				 * 当控件宽度不足以显示下一个单词时，使用“-”连接符连接单词换行显示
				 */
				// 如果不是复合词组
				// p--;// 前推切割点
				if(this.isLetter(str.charAt(p-2))){// 判断切割点前2位是否仍是英文字母
					str=this.insertChar(str,p-1,"-");// 将连接符插入
				}else{
					p--;
				}
			}
		}
		// 使至少移动一位，避免因为表格宽度过小导致位移为零从而陷入死循环
		if(p<=0) p=1;
		// 根据切割点将字符串str划分为两段，并返回
		var before=str.substring(0,p);
		var after=str.substring(p,str.length);
		return {"before":before,"after":after};
	},
	lineFeeds : function(mytable, row, col) {
		var cell = mytable.getCell(row, col);// 表格中的一个单元格
		var data = mytable.getData(row, col);// 该单元格的文本数据
		if(!data || data.length==0)return;// 如果单元格内无数据，返回
		var cellWidth = cell.width();// 单元格的像素宽度
		var after=data;
		var newData="";
		while(this.getWidth(after,cell)>cellWidth){
			var result=this.cutOneLine(after,cellWidth,cell);
			newData+=result.before+this.symbol;
			after=result.after;
		}
		newData+=after;
		cell.html(newData);
	}
};
