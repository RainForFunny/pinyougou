package com.pinyougou.search.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.search.service.BaseService;
import com.pinyougou.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.common.Mapper;

import java.io.Serializable;
import java.util.List;

public abstract class BaseServiceImpl<T> implements BaseService<T> {

    //在spring4.0以后才有的泛型依赖注入
    @Autowired
    private Mapper<T> mapper;


    /**
     * 根据主键查询
     *
     * @param id 主键
     * @return 实体类
     */
    @Override
    public T findOne(Serializable id) {
        return mapper.selectByPrimaryKey(id);
    }

    /**
     * 查询全部
     *
     * @return 实体类列表
     */
    @Override
    public List<T> findAll() {
        return mapper.selectAll();
    }

    /**
     * 条件查询
     *
     * @param t 条件对象
     * @return 实体类
     */
    @Override
    public List<T> findWhere(T t) {
        return mapper.select(t);
    }

    /**
     * 分页查询
     *
     * @param page 当前页号
     * @param rows 页面大小
     * @return 分页对象（列表，总记录数）
     */
    @Override
    public PageResult findPage(Integer page, Integer rows) {
        //设置分页
        PageHelper.startPage(page,rows);

        //查询
        List<T> list = mapper.selectAll();

        //封装进pageInfo
        PageInfo<T> pageInfo = new PageInfo<>(list);

        return new PageResult(pageInfo.getTotal(),pageInfo.getList());
    }

    /**
     * 条件分页查询
     *
     * @param page 当前页号
     * @param rows 页面大小
     * @param t    条件
     * @return 分页对象（列表，总记录数）
     */
    @Override
    public PageResult findPage(Integer page, Integer rows, T t) {
        //设置分页
        PageHelper.startPage(page,rows);

        //查询
        List<T> list = mapper.select(t);

        //封装进pageInfo
        PageInfo<T> pageInfo = new PageInfo<>(list);

        return new PageResult(pageInfo.getTotal(),pageInfo.getList());
    }

    /**
     * 新增
     *
     * @param t 实体类
     */
    @Override
    public void add(T t) {
        //选择性新增：如果对象中没有设置值的那些属性，则不会在操作语句中出现
        //如如果只给name：insert into tb_brand(name) values(?)
        //如如果只给name,firstChar：insert into tb_brand(name, first_char) values(?,?)
        mapper.insertSelective(t);
    }

    /**
     * 更新
     *
     * @param t 条件
     */
    @Override
    public void update(T t) {
        //选择性更新：如果对象中没有设置值的那些属性，则不会在操作语句中出现
        //如如果只给id, name：update tb_brand set name =? where id=?
        //如如果只给id, name,firstChar：update tb_brand set name =?,first_char=? where id=?
        mapper.updateByPrimaryKeySelective(t);
    }

    /**
     * 根据主键批量删除
     *
     * @param ids 主键数组
     */
    @Override
    public void delByIds(Serializable[] ids) {
        if (ids != null && ids.length > 0){
            for (Serializable id : ids) {
                mapper.deleteByPrimaryKey(id);
            }
        }
    }
}
