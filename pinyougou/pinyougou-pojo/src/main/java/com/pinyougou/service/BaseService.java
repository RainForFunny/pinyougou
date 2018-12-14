package com.pinyougou.service;


import com.pinyougou.vo.PageResult;

import java.io.Serializable;
import java.util.List;

public interface BaseService<T> {

    /**
     * 根据主键查询
     * @param id    主键
     * @return      实体类
     */
    T findOne(Serializable id);

    /**
     * 查询全部
     * @return 实体类列表
     */
    List<T> findAll();

    /**
     * 条件查询
     * @param t 条件对象
     * @return 实体类
     */
    List<T> findWhere(T t);

    /**
     * 分页查询
     * @param page 当前页号
     * @param rows 页面大小
     * @return 分页对象（列表，总记录数）
     */
    PageResult findPage(Integer page, Integer rows);

    /**
     * 条件分页查询
     * @param page 当前页号
     * @param rows 页面大小
     * @param t 条件
     * @return 分页对象（列表，总记录数）
     */
    PageResult findPage(Integer page, Integer rows,T t);

    /**
     * 新增
     * @param t 实体类
     */
    void add(T t);

    /**
     * 更新
     * @param t 条件
     */
    void update(T t);

    /**
     * 根据主键批量删除
     * @param ids 主键数组
     */
    void delByIds(Serializable[] ids);
}
