//自定义服务层:参数一:服务层名称,参数二:服务层功能:发送请求:其实就是在controller层拼接
app.service("specificationService",function($http){
    //定义findAll的请求路径:
    this.findAll=function(){
        return $http.get("../specification/findAll.do");
    }
    //定义findPage
    this.findPage=function(pageNum,pageSize){
        return $http.get("../specification/findPage.do?pageNum=" + pageNum + "&pageSize=" + pageSize);
    }

    //定义条件分页查询:
    this.search=function(searchEntity,pageNum,pageSize){
        return $http.post("../specification/search.do?pageNum=" + pageNum + "&pageSize=" + pageSize,searchEntity);
    }
    //增加的方法:
    this.add=function(entity){
        return $http.post("../specification/add.do",entity);
    }
    //修改的方法:
    this.update=function(entity){
        return $http.post("../specification/update.do",entity);
    }
    //findOne
    this.findOne=function(id){
        return $http.get("../specification/findOne.do?id="+id);
    }
    //删除的方法:
    this.del=function(ids){
        return $http.get("../specification/del.do?ids="+ids);
    }
    //查询选项:
    this.selectSpecOptions=function(){
        return $http.get("../specification/selectSpecOptions.do");
    }

})