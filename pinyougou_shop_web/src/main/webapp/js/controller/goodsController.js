 //控制层 
app.controller('goodsController' ,function($scope,$controller,goodsService,itemCatService,typeTemplateService,uploadService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.goods.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			//增加之前将decription赋值:
			$scope.entity.goodsDesc.introduction = editor.html();
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//保存成功之后清空entity中的数据
					$scope.entity={};
                    $scope.itemList=[];
					editor.html("");
					alert("商品录入成功!");
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	//联动效果:上来就查询一级分类列表数据:
	$scope.selectItemCat1List=function(){
		itemCatService.findByParentId(0).success(function(response){
			$scope.itemCat1List = response;
		});
	}
	//监控上一级的值,如果变化.function
	$scope.$watch("entity.goods.category1Id",function(newValue,odlValue){
		itemCatService.findByParentId(newValue).success(function(response){
			$scope.itemCat2List=response;
            $scope.itemCat3List={};
		})
	})
	//监控第二级,显示给第三级;
	$scope.$watch("entity.goods.category2Id",function(newValue,oldValue){
        itemCatService.findByParentId(newValue).success(function(response){
            $scope.itemCat3List=response;
        })
	})
    //监控第三级,先查询出typeId,载通过typeId显示brand;
    $scope.$watch("entity.goods.category3Id",function(newValue,oldValue){

        itemCatService.findOne(newValue).success(function(response){
            $scope.entity.goods.typeTemplateId=response.typeId;
        })
    })
	//通过typeTemplateId查询template的brand
	$scope.$watch("entity.goods.typeTemplateId",function(newValue,oldValue){
		//因为brand在数据库中是json格式,所以fandOne就可以了
		typeTemplateService.findOne(newValue).success(function(response){
			$scope.brandList = JSON.parse(response.brandIds);
            //通过模板id查询模板中的扩展属性:
            $scope.entity.goodsDesc.customAttributeItems = JSON.parse(response.customAttributeItems);
		});
        typeTemplateService.selectSepcOptions(newValue).success(function(response){
        	$scope.specList = response;
		});
	})

	$scope.imageEntity={};
	//文件上传功能:
	$scope.uploadFile=function(){
        uploadService.uploadFile().success(function(response){
        	if(response.success){
                $scope.imageEntity.url = response.message;
			}else{
        		alert(response.message);
			}

		});
	}

	//其中images在goodsDesc中
	$scope.entity={goods:{isEnableSpec:"1"},goodsDesc:{itemImages:[],specificationItems:[]},itemList:[]};
	//将图片和颜色保存到数据库,数据库中是以[{},{}]形式保存的,我们保存的时候保存的是整个entity对象
	//保存图片:
	$scope.saveImage=function(){
		$scope.entity.goodsDesc.itemImages.push($scope.imageEntity);
	}
	//删除图片:
	$scope.deleImage=function(index){
        $scope.entity.goodsDesc.itemImages.splice(index,1);
	}

    $scope.updateSpecItems=function($event,name,value){
		//需要判断结果集里边是不是有name,有直接存,没有创建一个对象:
		//{"attributeName":"网络","attributeValue":["移动3G"]}
		var specItem = $scope.exitAttributeName($scope.entity.goodsDesc.specificationItems,"attributeName",name);
		if(specItem!=null){
			//对象已存在:
			//判断是勾选还是取消勾选:
			if($event.target.checked){
				//勾选:直接加
				specItem.attributeValue.push(value);
			}else{
				//取消勾选就splice
				var index = specItem.attributeValue.indexOf(value);  //获取当前值得下标
                specItem.attributeValue.splice(index,1);
                //如果移除之后,没有勾选项了,就直接删除此对象:
				if(specItem.attributeValue.length==0){
					//从specificationItems中获取当前specItem的下标
					var index2 = $scope.entity.goodsDesc.specificationItems.indexOf(specItem);
					//移除当前specItem
                    $scope.entity.goodsDesc.specificationItems.splice(index2,1);

				}
			}
		}else{
			//不存在直接创建;
            $scope.entity.goodsDesc.specificationItems.push({"attributeName":name,"attributeValue":[value]});
		}
	}

	//循环 $scope.entity.goodsDesc.specificationItems
	//声明一个数组:每一条组合都是数组中的一个对象:[{}{}]
	$scope.createItemList=function(){
		//初始化一个item数组(集合)html中准备遍历此数组:{"机身内存":"16G","网络":"联通3G"}
        $scope.itemList = [{"spec":{},"price":1000,"num":100,"status":"1","isDefault":"0"}];
		//给spec属性赋值:
        //[{"attributeName":"网络制式","attributeValue":["移动3G","移动4G"]},
        // {"attributeName":"屏幕尺寸","attributeValue":["6寸","5寸"]}]

		var specItems = $scope.entity.goodsDesc.specificationItems;
        if(specItems.length==0){
            $scope.itemList=[];
            $scope.entity.itemList=[];
        }
		for(var i=0;i<specItems.length;i++){
			//i=0的时候传过去的是{"attributeName":"网络制式","attributeValue":["移动3G","移动4G"]}
            $scope.itemList = $scope.addSpec( $scope.itemList,specItems[i]["attributeName"],specItems[i]["attributeValue"]);
            $scope.entity.itemList = $scope.itemList;
        }


	};
	//这里items = itemList,attributeValue是数组:
	$scope.addSpec=function(items,attributeName,attributeValue){
		//定义一个新的items
		var newList=[];
		//遍历items
		for(var i=0;i<items.length;i++){
			var oldItem = items[i];

			//需要向item中的spec对象赋值;
			for(var j=0;j<attributeValue.length;j++){
                //将原来的对象克隆下来:
                var newItem = JSON.parse(JSON.stringify(oldItem));  //这个代码使newItem内容等于oldItem
                //{"spec":{"网络":"移动3G"},"price":1000,"num":100,"status":"1","isDefault":"0"}
				newItem["spec"][attributeName]=attributeValue[j];
				//将newItem对象添加到newList中
				newList.push(newItem);
			}
		}
		return newList;
	};
	//定义一个申请状态的数组:
	$scope.status = ["未审核","通过","未通过","关闭"];
	//定义上下架状态数组:
    $scope.isMarketable = ["已下架","已上架"];
	//数据库的id作为数组索引,内容为索引值:
	$scope.itemCatList = [];
	$scope.createItemCatList = function(){
        itemCatService.findAll().success(function(response){
        	for(var i=0;i<response.length;i++){
                $scope.itemCatList[response[i].id] = response[i].name;
			}
		})
	};
	//商品上下架功能:
	$scope.updateIsMarketable=function(isMarketable){
		goodsService.updateIsMarketable($scope.selectIds,isMarketable).success(function(response){
            if(response.success){
                alert(response.message);
                $scope.reloadList();//重新加载
                //ids 必须设置为空;否则已审核的状态也会改变:
                $scope.selectIds = [];
            }else{
                alert(response.message);
            }
		});
	}

    
});	
