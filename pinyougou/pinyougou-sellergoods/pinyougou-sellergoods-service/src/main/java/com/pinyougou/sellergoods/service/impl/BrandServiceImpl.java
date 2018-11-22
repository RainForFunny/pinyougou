package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.BrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

//暴露服务，也就是将该服务注册到注册中心，并在ioc中存在该对象
@Service(interfaceClass = BrandService.class)
public class BrandServiceImpl implements BrandService {

    //品牌的mapper跟service在同一个ioc容器中
    @Autowired
    private BrandMapper brandMapper;

    /**
     * 查询所有品牌列表
     *
     * @return 品牌列表
     */
    @Override
    public List<TbBrand> queryAll() {
        return brandMapper.queryAll();
    }
}
