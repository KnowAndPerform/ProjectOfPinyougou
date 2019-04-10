app.controller("searchController",function ($scope,$controller,$location,searchService) {

    $controller("baseController",{$scope:$scope});



    //初始化resultMap
    $scope.searchMap = {
        keywords:"",
        category:"",
        brand:"",
        spec:{},  //是一个map对象:{key:value}
        price:"",
        sort:"ASC",      //排序方式(ASC,DESC)
        sortField:"",  //排序字段:(按什么排序)
        pageNo:1,
        pageSize:60


    };

    //$location用来接收页面的参数;
    var keywords = $location.search()["keywords"];
    if(keywords!="undefined"){
        $scope.searchMap.keywords = keywords;
    }

    //根据类别id查询所有广告数据;
    $scope.search=function(){

        searchService.searchItem($scope.searchMap).success(function(response){
           $scope.resultMap = response;
           //调用分页处理方法?
            buildPageLabel();
        });
    };
    //添加过滤条件:
    $scope.addFilterCondition=function(key,value){
        //查看是否是分类,品牌,价格?因为存储方式不一样:
        if(key=="category" || key=="brand" || key=="price"){
            $scope.searchMap[key] = value;  //字符串:
        }else{
            $scope.searchMap.spec[key] = value;
        }
        //加载search方法:
        $scope.search();
    };
    //删除过滤条件:
    $scope.removeSearchItem=function(key){
        //查看是否是分类,品牌,价格?因为存储方式不一样:
        if(key=="category" || key=="brand" || key=="price"){
            $scope.searchMap[key] = "";  //字符串:
        }else{
          delete  $scope.searchMap.spec[key];
        }
        //加载search方法:
        $scope.search();
    };
    //排序方法:
    $scope.sortSearch=function (sortField,sort){
        $scope.searchMap.sort = sort;
        $scope.searchMap.sortField = sortField;
        //加载search方法:
        $scope.search();
    };

    buildPageLabel=function(){
        $scope.pageLabel = [];// 新增分页栏属性
        var maxPageNo = $scope.resultMap.totalPages;// 得到最后页码

        // 定义属性,显示省略号
        $scope.firstDot = true;
        $scope.lastDot = true;

        var firstPage = 1;// 开始页码
        var lastPage = maxPageNo;// 截止页码

        if ($scope.resultMap.totalPages > 5) { // 如果总页数大于5页,显示部分页码
            if ($scope.resultMap.pageNo <= 3) {// 如果当前页小于等于3
                lastPage = 5; // 前5页
                // 前面没有省略号
                $scope.firstDot = false;

            } else if ($scope.searchMap.pageNo >= lastPage - 2) {// 如果当前页大于等于最大页码-2
                firstPage = maxPageNo - 4; // 后5页
                // 后面没有省略号
                $scope.lastDot = false;
            } else {// 显示当前页为中心的5页
                firstPage = $scope.searchMap.pageNo - 2;
                lastPage = $scope.searchMap.pageNo + 2;
            }
        } else {
            // 页码数小于5页  前后都没有省略号
            $scope.firstDot = false;
            $scope.lastDot = false;
        }
        // 循环产生页码标签
        for (var i = firstPage; i <= lastPage; i++) {
            $scope.pageLabel.push(i);
        }
    };


    //分页查询
    $scope.queryForPage=function(pageNo){
        $scope.searchMap.pageNo=pageNo;

        //执行查询操作
        $scope.search();

    };

    //分页页码显示逻辑分析：
    // 1,如果页面数不足5页,展示所有页号
    // 2,如果页码数大于5页
    // 1) 如果展示最前面的5页,后面必须有省略号.....
    // 2) 如果展示是后5页,前面必须有省略号
    // 3) 如果展示是中间5页,前后都有省略号


});