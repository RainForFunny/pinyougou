app.controller("baseController",function ($scope) {

//初始化分页参数
    $scope.paginationConf = {
        currentPage: 1,//当前页号
        totalItems: 10,//总记录数
        itemsPerPage: 10,//页大小
        perPageOptions: [10, 20, 30, 40, 50],//可选择的每页大小
        onChange: function () {//当上述的参数发生变化后触发
            $scope.reloadList();
        }
    };

// //重新加载列表
    $scope.reloadList = function () {
        //$scope.findPage($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);

    };

//定义一个选择的id的数组
    $scope.selectedIds = [];

//选择或反选
    $scope.updateSelection = function ($event, id) {
        //$event.target 最初触发事件的DOM元素。
        if ($event.target.checked) {
            //如果是选中复选框，应该将当前点击了的品牌的id 记录到 选择了的id数组中
            $scope.selectedIds.push(id);
        } else {
            //如果是反选复选框，应该将当前点击的品牌的id从 选择了的id数组中 删除
            var index = $scope.selectedIds.indexOf(id);
            //参数一：要删除的元素的索引号，参数二：删除的个数
            $scope.selectedIds.splice(index, 1);
        }
    };

    //将一个json列表字符串中的某个属性的值串起来返回
    $scope.jsonToString = function (jsonArrayStr, key) {
        var str = "";
        //将字符串转换为json
        var jsonArray = JSON.parse(jsonArrayStr);
        for (var i = 0; i < jsonArray.length; i++) {
            var jsonObj = jsonArray[i];
            if (str.length > 0) {
                str += "," + jsonObj[key];
            } else {
                str =jsonObj[key];
            }
        }
        return str;
    }
});

