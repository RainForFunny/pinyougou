package com.pinyougou.sellergoods.service;

import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.service.BaseService;
import com.pinyougou.vo.PageResult;
import com.pinyougou.vo.Result;
import com.pinyougou.vo.Specification;

import java.util.List;
import java.util.Map;

public interface SpecificationService extends BaseService<TbSpecification> {

    PageResult search(Integer page, Integer rows, TbSpecification specification);

    void add(Specification specification);

    Specification findOne(Long id);

    void update(Specification specification);

    void deleteSpecificationByIds(Long[] ids);

    /**
     * 查询规格列表
     * @return 规格列表 数据结构：[{"id":1,"text":"机身内存"},{"id":2,"text":"尺寸"}]
     */
    List<Map> selectOptionList();
}