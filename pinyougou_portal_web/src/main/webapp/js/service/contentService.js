app.service("contentService",function($http){

    //通过类别id查询所有广告
    this.findContentsByCategoryId=function(cid){
       return $http.get("./content/findContentsByCategoryId.do?cid="+cid);
    }
})