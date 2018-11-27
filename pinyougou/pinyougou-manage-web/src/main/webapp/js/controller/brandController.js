app.controller("brandController",function ($scope,$http,$controller,brandService) {

    //继承处理器，参数一：被继承的处理器名称，参数二：传递本处理器的信息到父处理器
    $controller("baseController",{$scope:$scope});

    //查询所有品牌列表数据并绑定到list对象
    $scope.findAll = function () {
        //webapp相当于http://localhost:9100
        brandService.findAll().success(function (response) {
            // console.log("response : " + response);
            $scope.list = response;
        });
    };

    //分页查询
    $scope.findPage = function (page, rows) {
        brandService.findPage(page,rows).success(function (response) {
            //更新记录列表
            $scope.list = response.rows;

            //更新记录总数
            $scope.paginationConf.totalItems = response.total;
        });
    };

    //新增/更新
    $scope.save = function () {
        var obj;
        if ($scope.entity.id != null) {
            obj = brandService.update($scope.entity);
        }else {
            obj = brandService.add($scope.entity);
        }
        obj.success(function (response) {
            if (response.success) {
                $scope.reLoadList();
            } else {
                alert(response.message);
            }
        })
    };

    //回显品牌，根据id查询品牌
    $scope.findOne = function (id) {
        brandService.findOne(id).success(function (response) {
            $scope.entity = response;
        })
    };

    //批量删除
    $scope.delete = function () {
        if ($scope.selectedIds.length < 1) {
            alert("请选择要删除的记录。");
            return;
        }

        if (confirm("确定要删除选择的记录吗？")){
            brandService.delete($scope.selectedIds).success(function (response) {
                if (response.success) {
                    $scope.reLoadList();
                    $scope.selectedIds = [];
                } else {
                    alert(response.message);
                }
            })
        }
    };

    //根据品牌名称、首字母模糊分页查询品牌数据返回分页对象
    //定义一个空的搜索条件对象（品牌json格式字符串）
    $scope.searchEntity = {};

    $scope.search = function (page,rows) {
        brandService.search(page,rows,$scope.searchEntity).success(function (response) {
            $scope.list = response.rows;
            $scope.paginationConf.totalItems = response.total;
        })
    }
});