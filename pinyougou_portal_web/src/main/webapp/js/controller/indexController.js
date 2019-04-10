app.controller("indexController",function ($scope,$controller,contentService) {

    $controller("baseController",{$scope:$scope});

    //根据类别id查询所有广告数据;
    $scope.findContentsByCategoryId=function(cid){

        contentService.findContentsByCategoryId(cid).success(function(response){
           $scope.contentList = response;
        });
    };

    //portal搜索跳转到search.pinyougou.com/search.html
    $scope.search=function(){
        //#  angularjs中页面传参需要路由机制;
        location.href="http://search.pinyougou.com/search.html#?keywords="+$scope.keywords;
    }
});