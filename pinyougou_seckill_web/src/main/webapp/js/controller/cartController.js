app.controller('cartController',function($scope,$controller,cartService){
    //继承baseController,共享$scope内置对象
    $controller('baseController',{$scope:$scope});

    //查询购物车列表:
    $scope.findCartList=function () {
        cartService.findCartList().success(function (response) {
            $scope.cartList = response;
            sum();
        });
    };

    //添加购物车商品
    $scope.addItemToCartList=function(itemId,num){
        cartService.addItemToCartList(itemId,num).success(function (response) {
            if(response.success){
                /*alert(response.message);*/
                $scope.findCartList();
            }else{
                alert(response.message);
            }
        });
    }
    //计算商品数量和总金额:
    sum=function () {
         $scope.totalNum = 0;
         $scope.totalMoney = 0;
         for(var i=0;i<$scope.cartList.length;i++){
             for(var j=0;j<$scope.cartList[i].orderItemList.length;j++){
                 $scope.totalNum += $scope.cartList[i].orderItemList[j].num;
                 $scope.totalMoney += $scope.cartList[i].orderItemList[j].totalFee;
             }
         }
    };
});
