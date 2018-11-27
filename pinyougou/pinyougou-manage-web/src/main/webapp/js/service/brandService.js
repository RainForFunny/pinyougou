app.service("brandService",function ($http) {
    //查询所有品牌列表数据并绑定到list对象
    this.findAll = function () {
        //webapp相当于http://localhost:9100
        return $http.get("../brand/findAll.do");
    };


    //分页查询
    this.findPage = function (page, rows) {
        return $http.get("../brand/findPage.do?page=" + page + "&rows=" + rows);
    };

    //新增
    this.add = function (entity) {
        return $http.post("../brand/add.do",entity);
    };

    //更新
    this.update = function (entity) {
        return $http.post("../brand/update.do",entity);
    };

    //回显品牌，根据id查询品牌
    this.findOne = function (id) {
        return $http.get("../brand/findOne.do?id=" + id);
    };

    //批量删除
    this.delete = function (selectedIds) {
        return $http.get("../brand/delete.do?ids=" + selectedIds);
    };

    //根据品牌名称、首字母模糊分页查询品牌数据返回分页对象

    this.search = function (page,rows,searchEntity) {
        return $http.post("../brand/search.do?page=" + page + "&rows=" + rows,searchEntity);
    }

    //获取格式化的品牌列表
    this.selectOptionList = function () {
        return $http.get("../brand/selectOptionList.do");

    };
});