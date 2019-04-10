app.controller("indexController",function($scope,$controller,loginService){

    //控制器的继承关系:
    $controller("baseController",{$scope:$scope});

    $scope.getLoginName=function(){
        loginService.getLoginName().success(function(response){
            $scope.loginName=response.loginName;
        })
    }
})