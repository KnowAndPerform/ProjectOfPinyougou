app.service("seckillService",function ($http) {

    this.findSeckillGoods=function(){
        return $http.get("seckill/displaySeckillGoods.do");
    }
    this.findOne=function(seckillGoodsId){
        return $http.get("seckill/findOne.do?seckillGoodsId="+seckillGoodsId);
    }
    //抢单:
    this.createOrder=function(seckillGoodsId){
        return $http.get("seckill/createOrder.do?seckillGoodsId="+seckillGoodsId);
    }
});