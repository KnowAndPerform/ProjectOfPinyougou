//服务层
app.service('uploadService',function($http){
	    	
	//读取列表数据绑定到表单中
	this.uploadFile=function(){
		//通过表单数据对象上传文件:
		var formData = new FormData();
		//参数1:后端接收文件的形参.
		//参数2:file是文件上传处的id,files[0]是第一个文件;
		formData.append("file",file.files[0]);
		return $http({
			url:"../upload/uploadFile.do",   //提交的路径;
			method:"post",    //提交方式必须为post
			data:formData,   //提交的数据,被formData封装好了,
            headers : {'Content-Type' : undefined}, //上传文件必须是这个类型，默认text/plain 											，相当于指定form的enctype="multipart/form-data"属性
            transformRequest : angular.identity  //对整个表单进行二进制序列化
		});
	}

});
