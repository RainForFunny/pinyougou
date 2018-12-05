package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.SpecificationOptionMapper;
import com.pinyougou.mapper.TypeTemplateMapper;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.pojo.TbTypeTemplate;
import com.pinyougou.sellergoods.service.TypeTemplateService;
import com.pinyougou.service.impl.BaseServiceImpl;
import com.pinyougou.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.Map;

@Service(interfaceClass = TypeTemplateService.class)
public class TypeTemplateServiceImpl extends BaseServiceImpl<TbTypeTemplate> implements TypeTemplateService {

    @Autowired
    private TypeTemplateMapper typeTemplateMapper;

    @Autowired
    private SpecificationOptionMapper specificationOptionMapper;

    @Override
    public PageResult search(Integer page, Integer rows, TbTypeTemplate typeTemplate) {
        PageHelper.startPage(page, rows);

        Example example = new Example(TbTypeTemplate.class);
        Example.Criteria criteria = example.createCriteria();
        if(!StringUtils.isEmpty(typeTemplate.getName())){
            criteria.andLike("name", "%" + typeTemplate.getName() + "%");
        }

        List<TbTypeTemplate> list = typeTemplateMapper.selectByExample(example);
        PageInfo<TbTypeTemplate> pageInfo = new PageInfo<>(list);

        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public List<Map> findSpecList(Long id) {
        //根据分类模板id查询分类模板
        TbTypeTemplate template = findOne(id);
        if (!StringUtils.isEmpty(template.getSpecIds())) {
            //根据分类模板中的规格列表中的每个规格查询其对应的规格选项列表
            //规格列表[{"id":33,"text":"电视屏幕尺寸"]
            List<Map> specList = JSON.parseArray(template.getSpecIds(), Map.class);
            for (Map map : specList) {
                //根据规格id查询规格选项
                TbSpecificationOption param = new TbSpecificationOption();
                //查询条件：规格id
                param.setSpecId(Long.parseLong(map.get("id").toString()));

                List<TbSpecificationOption> options = specificationOptionMapper.select(param);

                map.put("options", options);
            }

            //返回期望的数据
            return specList;
        }
        return null;
    }
}
