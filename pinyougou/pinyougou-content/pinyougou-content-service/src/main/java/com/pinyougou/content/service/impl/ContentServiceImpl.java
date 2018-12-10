package com.pinyougou.content.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.ContentMapper;
import com.pinyougou.pojo.TbContent;
import com.pinyougou.content.service.ContentService;
import com.pinyougou.search.service.impl.BaseServiceImpl;
import com.pinyougou.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service(interfaceClass = ContentService.class)
public class ContentServiceImpl extends BaseServiceImpl<TbContent> implements ContentService {

    private static final String CONTENT = "content";
    @Autowired
    private ContentMapper contentMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public PageResult search(Integer page, Integer rows, TbContent content) {
        PageHelper.startPage(page, rows);

        Example example = new Example(TbContent.class);
        Example.Criteria criteria = example.createCriteria();
        /*if(!StringUtils.isEmpty(content.get***())){
            criteria.andLike("***", "%" + content.get***() + "%");
        }*/

        List<TbContent> list = contentMapper.selectByExample(example);
        PageInfo<TbContent> pageInfo = new PageInfo<>(list);

        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public List<TbContent> findContentListByCategoryId(Long categoryId) {
        List<TbContent> contentList = null;
        try {
            //1. 先从redis查询内容列表，如果找到则直接返回；
            contentList = (List<TbContent>) redisTemplate.boundHashOps(CONTENT).get(categoryId);
            if (contentList != null) {
                return contentList;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        /**
         * --查询内容分类id为1并且有效的广告并且按照排序字段降序排序
         * select * from tb_content where category_id=? and status=1 order by sort_order desc
         */
        Example example = new Example(TbContent.class);
        Example.Criteria criteria = example.createCriteria();
        //条件
        criteria.andEqualTo("categoryId",categoryId);

        //有效状态
        criteria.andEqualTo("status","1");

        //降序排序
        example.orderBy("sortOrder").desc();

        contentList = contentMapper.selectByExample(example);
        //2. 如果在redis中不存在内容列表，则从mysql根据条件查询；返回数据之前将数据存入到redis
        try {
            redisTemplate.boundHashOps(CONTENT).put(categoryId,contentList);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return contentList;
    }

}
