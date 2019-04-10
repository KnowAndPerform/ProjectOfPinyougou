app.controller('payController',function($scope,$controller,$location,payService){
    //继承baseController,共享$scope内置对象
    $controller('baseController',{$scope:$scope});

    //生成支付码:
    $scope.createNative=function () {
        payService.createNative().success(function (response) {
            //支付订单号:
            $scope.out_trade_no = response.out_trade_no;
            //支付总金额:(元,两位小数)
            $scope.total_fee = (response.total_fee/100).toFixed(2);
            //生成对应此订单的二维码;
            var qr = window.qr = new QRious({
                element: document.getElementById('qrious'),
                size: 250,
                value: response.code_url,
                level:'H'
            })
            //生成二维码后就开始查询订单状态是否被支付;
            $scope.queryPayStatus();
        })
    };

    //查询支付状态
    $scope.queryPayStatus=function(){
        payService.queryPayStatus($scope.out_trade_no).success(function (response) {
            if(response.success){
                location.href="paysuccess.html#?payMoney="+$scope.total_fee;
            }else{
                if(response.message=="timeout"){
                    $scope.createNative();  //支付超时重新生成订单;
                }else{
                    location.href="payfail.html";
                }
            }
        });
    }
    //页面传参的获取:
    $scope.getMoney=function () {
        $scope.payMoney = $location.search()["payMoney"];
    }
});
