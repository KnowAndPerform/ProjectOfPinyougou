 //控制层 
app.controller('typeTemplateController' ,function($scope,$controller ,brandService,specificationService,typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		typeTemplateService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		typeTemplateService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		typeTemplateService.findOne(id).success(
			function(response){
				$scope.entity= response;

                //因为只能回显一个电脑,但是不是有fastjson解析了么,为什么还需要在这里转换啊?
                $scope.entity.brandIds =JSON.parse(response.brandIds);//解析品牌
                $scope.entity.specIds =JSON.parse(response.specIds);//解析规格
                $scope.entity.customAttributeItems =JSON.parse(response.customAttributeItems);//解析扩展属性

			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=typeTemplateService.update( $scope.entity ); //修改  
		}else{
			serviceObject=typeTemplateService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	$scope.reloadList();//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框
		if(confirm("您确定要删除么?")){
            typeTemplateService.dele( $scope.selectIds ).success(
                function(response){
                    if(response.success){
                        $scope.reloadList();//刷新列表
                    }
                }
            );
        }

	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索=-=>条件分页查询;
	$scope.search=function(page,rows){			
		typeTemplateService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	//声明brandList为一个对象:对象中时key:value的格式,
	$scope.brandList={
		/*将数据库中的品牌数据,以json 的格式封装给data*/
	/*	data:[{id:1,text:'bug'},{id:2,text:'duplicate'},{id:3,text:'invalid'},{id:4,text:'wontfix'}]*/
	data:[]
	};
	//控制层方法中调用service层的方法  控制层处理后端返回数据,service调用后端方法
	$scope.selectBrandOptions=function(){
        brandService.selectBrandOptions().success(function(response){
            $scope.brandList.data = response;
        });
	};
	//声明specList对象:
	$scope.specList={
		data:[]
	};
	$scope.selectSpecOptions=function(){
        specificationService.selectSpecOptions().success(function(response){
            $scope.specList.data=response;
        });
	}
	//声明entity
	$scope.entity={customAttributeItems:[]};
	//增加行:
	$scope.addRow=function(){
		//向数组中添加对象:
        $scope.entity.customAttributeItems.push({});
	}
	//删除行:
    $scope.deleRow=function(index){
		//删除对象;
        $scope.entity.customAttributeItems.splice(index,1);
    }
    
});	
