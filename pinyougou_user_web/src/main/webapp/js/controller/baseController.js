//把controller中的共同代码抽取出来:
app.controller("baseController",function($scope){
    //定义分页控件对象:
    $scope.paginationConf = {
        currentPage:1,  				//当前页
        totalItems:10,					//总记录数
        itemsPerPage:10,				//每页记录数
        perPageOptions:[10,20,30,40,50], //分页选项，下拉选择一页多少条记录
        onChange:function(){			//页面变更后触发的方法
            $scope.reloadList();		//启动就会调用分页组件
        }
    };

    $scope.reloadList=function () {
        $scope.search($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);
    }

    /*声明存放复选框的ids*/
    $scope.selectIds = [];
    /*向数组中添加所选中的复选框*/
    $scope.updateSelection = function ($event, id) {
        if ($event.target.checked) {
            /*被勾选添加*/
            $scope.selectIds.push(id);
        }else{
            var index = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(index,1);//index数组的下标,从此下标删除1个元素
        }
    }
    /**
     * 将json字符串中的Map数据解析出来:
     *
     */
    $scope.getValueByKey=function(jsonString,key){
        //解析json数组格式字符串，获取json数组中对象的属性值，做字符串拼接
        // 将json数组字符串解析成数组:w
        // "[{"id":26,"text":"尺码"},{"id":37,"text":"颜色"}]"==>
        // [{"id":26,"text":"尺码"},{"id":37,"text":"颜色"}]
        var array = JSON.parse(jsonString);
        var value="";
        for(var i=0;i<array.length;i++){
            if(i>0){
                //基于json对象的属性名获取属性值有两种方式
                //1、如果属性名是确定值， 获取方式：对象.属性名  对象[属性名]
                //2、如果属性名是变量， 获取方式：对象[属性名]
                value+=","+array[i][key];
            }else{
                value+=array[i][key];
            }

        }
        return value;
    }
})
