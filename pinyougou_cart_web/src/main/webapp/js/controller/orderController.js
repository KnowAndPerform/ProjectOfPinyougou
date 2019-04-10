app.controller('orderController',function($scope,$controller,cartService,addressService,orderService){
    //继承baseController,共享$scope内置对象
    $controller('baseController',{$scope:$scope});

    //指定收件人地址:
    $scope.addresseeAddr = null;
    //提交后台的订单数据:
    $scope.entity = {"paymentType":'1'};
    //通过userId就是用户名而已哦!!!!查询address
    $scope.findAddressByUserId=function(){
      addressService.findAddressByUserId().success(function (response) {
            $scope.addressList = response;
            //遍历所有地址,页面刷新确定收件人地址为默认地址./第一个
          for(var i=0;i<$scope.addressList.length;i++){
              if($scope.addressList[i].isDefault=='1'){
                  $scope.addresseeAddr = $scope.addressList[i];
                  //赋值过后跳出循环;
                  break;
              }
          }
          if($scope.addresseeAddr==null){
              //如果没有设置默认地址,那么第一个为默认地址;和收件人地址;
              $scope.addresseeAddr = $scope.addressList[0];
          }
      });
    };

    //勾选为收货地址;
    $scope.isSelected=function(address){
      if($scope.addresseeAddr == address){
          return true;
      }else{

          return false;
      }
    };

    //勾选:
    $scope.updateSelect=function(address){
        $scope.addresseeAddr = address;
    };

    //更新支付方式;
    $scope.updatePaymentType=function(type){
        $scope.entity.paymentType=type;
    };

    //查询购物车列表:
    $scope.findCartList=function () {
        cartService.findCartList().success(function (response) {
            $scope.cartList = response;
            sum();
        });
    };

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

    //提交订单:
    $scope.submitOrder=function(){
        //封装后台需要提交的数据:
        $scope.entity.receiverAreaName = $scope.addresseeAddr.address;
        $scope.entity.receiverMobile = $scope.addresseeAddr.mobile;
        $scope.entity.receiver = $scope.addresseeAddr.contact;

        orderService.submitOrder($scope.entity).success(function(response){
            if(response.success){
                if($scope.entity.paymentType=='1'){
                    //成功后跳转到微信支付页面:
                    location.href="pay.html";
                }else{
                    location.href="paysuccess.html";
                }
            }else{
                alert(response.message);
            }
        });
    }
});
