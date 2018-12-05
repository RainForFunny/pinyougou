app.controller("searchController",function ($scope, searchService) {

    //定义提交到后台的对象
    $scope.searchMap = {"keywords":""};

    $scope.search = function () {
        searchService.search($scope.searchMap).success(function (response) {

            $scope.resultMap = response;
        })
    }
});