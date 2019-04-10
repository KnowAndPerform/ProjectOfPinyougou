app.controller("indexController",function ($scope,$controller,contentService) {

    $controller("baseController",{$scope:$scope});

    //根据类别id查询所有广告数据;
    $scope.findContentsByCategoryId=function(cid){

        contentService.findContentsByCategoryId(cid).success(function(response){
           $scope.contentList = response;
        });
    }
})