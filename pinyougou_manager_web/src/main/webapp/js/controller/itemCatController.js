 //控制层 
app.controller('itemCatController' ,function($scope,$controller   ,itemCatService,typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		itemCatService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		itemCatService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		itemCatService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=itemCatService.update( $scope.entity ); //修改  
		}else{
			//为entity付父类id
			$scope.entity.parentId=$scope.parentId;
			serviceObject=itemCatService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	$scope.findByParentId($scope.entity.parentId);//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(parentId){
		//获取选中的复选框
		if(confirm("您确定要删除么?")){
            itemCatService.dele( $scope.selectIds ).success(
                function(response){
                    if(response.success){
                        $scope.findByParentId(parentId);//刷新列表
                    }
                }
            );
		}

	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		itemCatService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	$scope.list = [];
	/*商品目录:*/
	$scope.findByParentId=function(parentId){
        itemCatService.findByParentId(parentId).success(function(response){
            $scope.parentId=parentId;
        	$scope.list = response;
		});
	}
	//定义本业商品的等级:
	$scope.grade=1;
	$scope.setGrade=function(grade){
		$scope.grade = grade;
	}
	$scope.parentId=0;
	//查询子类,传来对象的id作为父id,显示等级:
	$scope.selectList=function(entity_parent){
		if($scope.grade==1){
			//上方显示0层.
			$scope.entity_1=null;
			$scope.entity_2=null;
		}
		if($scope.grade==2){
			//显示第一层
            $scope.entity_1=entity_parent;
            $scope.entity_2=null;
		}
		if($scope.grade==3){
			//显示第二层
            $scope.entity_2=entity_parent;
		}
		//查询出子类:
        $scope.findByParentId(entity_parent.id);
	}
    $scope.templateList=[];
	$scope.seletTemplateList=function(){
        typeTemplateService.findAll().success(function(response){
        	$scope.templateList=response;
		});
	}
    
});	
