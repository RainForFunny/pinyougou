package com.pinyougou.sellergoods.service;

import com.pinyougou.pojo.TbBrand;
import com.pinyougou.service.BaseService;
import com.pinyougou.vo.PageResult;

import java.util.List;
import java.util.Map;

public interface BrandService extends BaseService<TbBrand> {

    /**
     * 查询所有品牌列表
     * @return 品牌列表
     */
    List<TbBrand> queryAll();

    /**
     * 根据当前页和页面大小查询品牌列表
     * @param page 当前页
     * @param rows 页面大小
     * @return     品牌列表
     */
    List<TbBrand> testPage(Integer page, Integer rows);

    /**
     * 条件分页查询
     * @param brand 查询条件对象
     * @param page  当前页号
     * @param rows  总记录数
     * @return
     */
    PageResult search(TbBrand brand, Integer page, Integer rows);


    /**
     * 查询品牌列表
     * @return 品牌列表 数据结构为：[{"id":1,"text":"联想"},{"id":2,"text":"华为"}]
     */
    List<Map> selectOptionList();
}
