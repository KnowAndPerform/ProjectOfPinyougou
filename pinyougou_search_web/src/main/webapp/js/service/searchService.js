app.service("searchService",function($http){

    //通过类别id查询所有广告
    this.searchItem=function(searchMap){
       return $http.post("./search/searchItem.do",searchMap);
    }
})