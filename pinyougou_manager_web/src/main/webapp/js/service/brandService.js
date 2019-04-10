//自定义服务层:参数一:服务层名称,参数二:服务层功能:发送请求:其实就是在controller层拼接
app.service("brandService", function ($http) {
    //定义findAll的请求路径:
    this.findAll = function () {
        return $http.get("../brand/findAll.do");
    }
    //定义findPage
    this.findPage = function (pageNum, pageSize) {
        return $http.get("../brand/findPage.do?pageNum=" + pageNum + "&pageSize=" + pageSize);
    }
    //定义条件分页查询:
    this.search = function (searchEntity, pageNum, pageSize) {
        return $http.post("../brand/search.do?pageNum=" + pageNum + "&pageSize=" + pageSize, searchEntity);
    }
    //增加的方法:
    this.add = function (entity) {
        return $http.post("../brand/add.do", entity);
    }
    //修改的方法:
    this.update = function (entity) {
        return $http.post("../brand/update.do", entity);
    }
    //findOne
    this.findOne = function (id) {
        return $http.get("../brand/findOne.do?id=" + id);
    }
    //删除的方法:
    this.del = function (ids) {
        return $http.get("../brand/del.do?ids=" + ids);
    }
    this.selectBrandOptions = function(){
        return $http.get("../brand/selectBrandOptions.do");
    }

})