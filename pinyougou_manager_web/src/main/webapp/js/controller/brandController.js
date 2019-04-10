/*controller的功能,为了继承父控制器,需要$controller域*/
app.controller("brandController", function ($scope, $controller,brandService) {
    //继承父控制器:参数1:父控制器的名称,参数2:固定写法,与父控制器共享$controller
    $controller("baseController",{$scope:$scope});

    //1,是查询数据库,获取所有的brands
    $scope.findAll = function () {
        //没有对象,所以用get就可以,这个是异步请求:直接调用service就可以了
        brandService.findAll().success(function (response) {
            //将请求来的list集合给$scope.list  全局域中的list
            $scope.list = response;
        });
    };




    $scope.findPage = function (pageNum, pageSize) {
        brandService.findPage(pageNum, pageSize).success(function (response) {
            //返回的是当前页面:
            $scope.list = response.rows;
            //总记录数:
            $scope.paginationConf.totalItems = response.total;
        })
    }
    //调用service层的search分页查询**********
    $scope.searchEntity={};

    $scope.search=function (pageNum,pageSize) {
        brandService.search($scope.searchEntity,pageNum,pageSize).success(function (response) {
            $scope.list=response.rows;//当前页结果集
            $scope.paginationConf.totalItems=response.total;//总记录数
        })
    }

    $scope.entity = {};

    /*保存功能:*/
    $scope.save = function () {
        var method={};
        if ($scope.entity.id == null) {
            method = brandService.add($scope.entity);
        } else {
            method = brandService.update($scope.entity);
        }
        //保存而已,没必要写俩啊
        method.success(function (response) {
            if (response.success) {
                $scope.reloadList();  //成功重新加载数据
            } else {
                alert(response.message);
            }
        })


    }
    /*findOneby id*/
    $scope.findOne = function (id) {
        brandService.findOne(id).success(function (response) {
            $scope.entity = response;
        })
    }


    /*删除方法*/
    $scope.del = function () {
        if (confirm("您确定要删除么?")) {
            brandService.del($scope.selectIds).success(function (response) {
                if (response.success) {
                    $scope.reloadList();  //成功重新加载数据
                    $scope.selectIds = [];   //清空数组
                } else {
                    alert(response.message);
                }
            })
        }

    }
})