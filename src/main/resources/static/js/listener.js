$(function() {
	// 选择单元格
	$("#mytable").on(
			"click",
			"td",
			function() {
				// 移除选中样式
				$(".selectCell").removeClass("selectCell");
				// 将该单元格及行数列数放入mytable对象中
				mytable.selectOne = $(this);
				mytable.selectRow = $(this).parent().index();
				mytable.selectCol = $(this).index();
				// 得到选择作用域
				var scope = $("#selectScope").val();
				// 给单元格添加选中样式
				if (scope == 0) {// 格
					mytable.selectOne.addClass("selectCell");
				} else if (scope == 1) {// 行
					mytable.selectOne.parent().addClass("selectCell");
				} else if (scope == 2) {// 列
					var cells = mytable.getColCells(mytable.selectCol);
					for (var i = 0; i < cells.length; i++) {
						cells[i].addClass("selectCell");
					}
				} else if (scope == 3) {// 表
					mytable.mytable.addClass("selectCell");
				}

				// 改变height和width文本框的内容为选中单元格的高度宽度
				$("#height").val(mytable.selectOne.height());
				$("#width").val(mytable.selectOne.width());
				// 改变wordsize下拉框内容为选中单元格的字体大小
//				$("#wordsize").find("option:contains(" + mytable.selectOne.css("font-size")+ ")").attr("selected", true);
				
				$("#wordsize").val(parseInt(mytable.selectOne.css("font-size")));
				$("#wordfont").val(mytable.selectOne.css("font-family"));
			});
	// 点击提交按钮，上传文件
	$("#uploadAndImportBtn").click(function() {
		var file=$("#file");
		if(file.val()==""||file.val()==null){
			alert("文件为空！");
			return;
		}
		var url="/upload.do";
		// 上传文件
		$.ajax({
			type : "POST",
			url : url,
			contentType : false,
			processData : false,
			data : new FormData($("#fileForm")[0]),
			success : function(data) {
				// 上传成功则载入文件
				if (data != "上传失败！") {
					filePath = data;
					// 加载表格
					importExcel(filePath);
					// 关闭模态框
					$("#myModal").modal("hide");
				} else {
					alert(data);
				}
			}
		});
	});
	// 点击下载按钮，验证文件是否存在，存在则下载，不存在则提示
	$("#downloadDiv").on("click","input[type=button]",function(){
		var url = $(this).parent().find("input[type='hidden']").val();
		if (isFileExists(url)) {
			$(this).parent().submit();
		} else {
			alert("文件不存在！");
		}
	});
	// 修改列宽,实时监听：input propertychange，丢掉焦点时监听：change
	$("#width").on("change", function() {
		if (mytable.selectOne == null || mytable.selectCol < 0) {
			alert("未选择单元格！");
			return;
		}
		var scope = $("#selectScope").val();
		if (scope == 0) {// 格
			mytable.changeWidth(mytable.selectCol, $(this).val());
		} else if (scope == 1) {// 行
			for (var i = 0; i < mytable.colNum; i++) {
				mytable.changeWidth(i, $(this).val());
			}
		} else if (scope == 2) {// 列
			mytable.changeWidth(mytable.selectCol, $(this).val());
		} else if (scope == 3) {// 表
			for (var i = 0; i < mytable.colNum; i++) {
				mytable.changeWidth(i, $(this).val());
			}
		}
	});
	// 修改行高
	$("#height").on("change", function() {
		if (mytable.selectOne == null || mytable.selectRow < 0) {
			alert("未选择单元格！");
			return;
		}
		var scope = $("#selectScope").val();
		if (scope == 0) {// 格
			mytable.changeHeight(mytable.selectRow, $(this).val());
		} else if (scope == 1) {// 行
			mytable.changeHeight(mytable.selectRow, $(this).val());
		} else if (scope == 2) {// 列
			for (var i = 0; i < mytable.rowNum; i++) {
				mytable.changeHeight(i, $(this).val());
			}
		} else if (scope == 3) {// 表
			for (var i = 0; i < mytable.rowNum; i++) {
				mytable.changeHeight(i, $(this).val());
			}
		}
	});
	// 修改字体大小
	$("#wordsize").on("change", function() {
		if (mytable.selectOne == null) {
			alert("未选择单元格！");
			return;
		}
		var scope = $("#selectScope").val();
		if (scope == 0) {// 格
			mytable.selectOne.css("font-size", $(this).val() + "px");
			//mytable.reWidth(mytable.selectRow,mytable.selectCol);
		} else if (scope == 1) {// 行
			var cells = mytable.getRowCells(mytable.selectRow);
			for (var i = 0; i < cells.length; i++) {
				cells[i].css("font-size", $(this).val() + "px");
				//mytable.reWidth(mytable.selectRow,i);
			}
		} else if (scope == 2) {// 列
			var cells = mytable.getColCells(mytable.selectCol);
			for (var i = 0; i < cells.length; i++) {
				cells[i].css("font-size", $(this).val() + "px");
				//mytable.reWidth(i,mytable.selectCol);
			}
		} else if (scope == 3) {// 表
			var cells = mytable.getAllCells();
			for (var i = 0; i < cells.length; i++) {
				cells[i].css("font-size", $(this).val() + "px");
			}
			//mytable.reWidth();
		}
	});
	// 修改字体
	$("#wordfont").on("change", function() {
		if (mytable.selectOne == null) {
			alert("未选择单元格！");
			return;
		}
		var scope = $("#selectScope").val();
		if (scope == 0) {// 格
			mytable.selectOne.css("font-family", $(this).val());
		} else if (scope == 1) {// 行
			var cells = mytable.getRowCells(mytable.selectRow);
			for (var i = 0; i < cells.length; i++) {
				cells[i].css("font-family", $(this).val());
			}
		} else if (scope == 2) {// 列
			var cells = mytable.getColCells(mytable.selectCol);
			for (var i = 0; i < cells.length; i++) {
				cells[i].css("font-family", $(this).val());
			}
		} else if (scope == 3) {// 表
			var cells = mytable.getAllCells();
			for (var i = 0; i < cells.length; i++) {
				cells[i].css("font-family", $(this).val());
			}
		}
	});
	// 切换显示的表
	$("#workSheet").on("change", function() {
		// 隐藏旧表
		mytable.mytable.hide();
		mytable = tableList[$(this).val()];
		// 显示新表
		mytable.mytable.show();
	});
	//打开BDF生成工具
	$("#BDFToolBtn").on("click", function() {
		$.ajax({
			type : "POST",
			url : "openBDFExe.do",
			success : function(data) {
				if(data==false)
					alert("BDF生成工具不存在！");
			}
		});
	});
	//打开BDF查看工具
	$("#BDFReaderBtn").on("click", function() {
		$.ajax({
			type : "POST",
			url : "openBDFReaderExe.do",
			success : function(data) {
				if(data==false)
					alert("BDF查看工具不存在！");
			}
		});
	});
});