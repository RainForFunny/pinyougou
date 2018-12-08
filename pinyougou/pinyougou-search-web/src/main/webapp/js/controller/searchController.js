app.controller("searchController",function ($scope,$location, searchService) {

    //定义提交到后台的对象
    $scope.searchMap = {"keywords":"","category":"","brand":"","spec":{},"price":"","pageNo":"1","pageSize":"20","sortField":"","sort":""};

    $scope.search = function () {
        searchService.search($scope.searchMap).success(function (response) {

            $scope.resultMap = response;

            //构建分页导航条
            buildPageInfo();
        })
    };
    
    //添加过滤条件查询
    $scope.addSearchItem = function (key,value) {
        if ("category" == key || "brand" == key || "price" == key){
            //如果点击的是分类或者品牌或价格的话
            $scope.searchMap[key] = value;
        } else {
            //如果点击的是规格的话
            $scope.searchMap.spec[key] = value;
        }

        //过滤后重新搜索
        $scope.search();
    };

    //撤销过滤条件
    $scope.removeSearchItem = function (key) {
        if ("category" == key || "brand" == key || "price" == key) {
            $scope.searchMap[key] = '';
        } else {
            //删除规格对象的属性
            delete $scope.searchMap.spec[key];
        }

        $scope.search();
    };

    //创建分页导航条
    buildPageInfo = function () {
        //在页面中要显示的分页页号数组
        $scope.pageNoList = [];

        //要显示的总页号数
        var showPageNoTotal = 5;
        //起始页号
        var startPageNo = 1;
        //结束页号
        var endPageNo = $scope.resultMap.totalPages;
        //如果总页数大于要显示的总页号数,才会有如下操作，否则直接显示所有页号
        if ($scope.resultMap.totalPages > showPageNoTotal) {
            //当前页的左右间隔数
            var interval = Math.floor(showPageNoTotal / 2);

            //当前页-间隔
            startPageNo = parseInt($scope.searchMap.pageNo) - interval;
            endPageNo = parseInt($scope.searchMap.pageNo) + interval;

            //如果结束页号大于总页数
            if (endPageNo > $scope.resultMap.totalPages) {
                startPageNo = $scope.resultMap.totalPages - showPageNoTotal + 1;
                endPageNo = $scope.resultMap.totalPages;
            } else if (startPageNo < 1) {
                //如果起始页号小于1
                startPageNo = 1;
                endPageNo = showPageNoTotal;
            }
        }

        //分页导航条前面三个点
        $scope.frontDot = false;
        if (startPageNo > 1) {
            $scope.frontDot = true;
        }

        //后面三个点
        $scope.backDot = false;
        if (endPageNo < $scope.resultMap.totalPages) {
            $scope.backDot = true;
        }
        
        for (var i = startPageNo; i <= endPageNo; i++) {
            $scope.pageNoList.push(i);
        }
    };

    //判断是否为当前页
    $scope.isCurrentPage = function (pageNo) {
        return $scope.searchMap.pageNo == pageNo;
    }

    //根据页号查询
    $scope.queryByPageNo = function (pageNo) {
        if (pageNo > 0 && pageNo <= $scope.resultMap.totalPages) {
            //设置页号
            $scope.searchMap.pageNo = pageNo;
        }
        $scope.search();
    };

    //下一页
    $scope.nextPage = function () {
        $scope.queryByPageNo(parseInt($scope.searchMap.pageNo) + 1);
    };

    //排序
    $scope.sortSearch = function (sortField, sort) {
        $scope.searchMap.sortField = sortField;
        $scope.searchMap.sort = sort;

        $scope.search();
    };

    //加载搜索关键字并搜索
    $scope.loadSearch = function () {
        //获取到浏览器地址栏的请求参数，如http://search.pinyougou.com/search.html#?keywords=小米
        $scope.searchMap.keywords = $location.search()["keywords"];
    }

});