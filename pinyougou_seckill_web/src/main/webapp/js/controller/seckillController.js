app.controller("seckillController",function ($scope,$controller,$location,$interval,seckillService) {
    //继承baseController
    $controller("baseController",{$scope:$scope});

    //查询所有秒杀商品:
    $scope.findSeckillGoods=function () {
        seckillService.findSeckillGoods().success(function (response) {
            $scope.seckillGoodsList = response;
        });
    };

    //获取秒杀商品详情:
    $scope.findOne=function(){
        //获取路由传参的参数;:
        $scope.seckillGoodsId = $location.search()["seckillGoodsId"];
        //根据路由传参参数,获取商品详情:
        seckillService.findOne($scope.seckillGoodsId).success(function (response) {
            $scope.seckillGoods = response;
            //商品倒计时时间的计算:
            var nowTime = new Date().getTime();
            var endTime = new Date($scope.seckillGoods.endTime).getTime() ;
            //商品剩余秒杀时间:
            $scope.remainingTime = Math.floor((endTime-nowTime)/1000);
            //?天,小时:分钟:秒
            //加一个定时器:
            var time = $interval(function () {
                if( $scope.remainingTime>0){
                    $scope.remainingTime--;
                    //时间减1后需要重新计算,并且转换字符串:
                    $scope.remainingTimeStr = $scope.calculateTime($scope.remainingTime);
                }else{
                    //如果时间为0,则停止计时;
                    $interval.clear(time);
                }
            },1000);


        });
    };
    $scope.calculateTime=function (remainingTime) {
        var days = Math.floor(remainingTime/(3600*24));
        var hours = Math.floor((remainingTime-days*3600*24)/3600);
        var minutes = Math.floor((remainingTime-days*3600*24- hours*3600)/60);
        var seconds = remainingTime- days*3600*24- hours*3600-minutes*60;
        //转换成字符串:
        var remainingTimeStr = "";
        remainingTimeStr += days+"天 ";
        if(hours<10){
            hours = "0"+hours;
        }
        if(minutes<10){
            minutes = "0"+minutes;
        }
        if(seconds<10){
            seconds = "0"+seconds;
        }
        remainingTimeStr += hours + ":"+minutes + ":"+seconds;
        return remainingTimeStr;
    };

    //抢购生成订单:
    $scope.createOrder=function () {
        seckillService.createOrder($scope.seckillGoodsId).success(function (response) {
            //应该返回一个支付页面吧;老师没有做;
            if(response.success){
                location.href="pay.html";
            }else{
                alert(response.message);
            }
        });

    }



});