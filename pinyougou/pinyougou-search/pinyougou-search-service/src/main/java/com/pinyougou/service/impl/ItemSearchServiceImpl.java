package com.pinyougou.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service(interfaceClass =  ItemSearchService.class)
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public Map<String, Object> search(Map<String, Object> searchMap) {
        Map<String,Object> resultMap = new HashMap<>();
//        SimpleQuery query = new SimpleQuery();
        SimpleHighlightQuery query = new SimpleHighlightQuery();

        //处理关键字中的空格
        if (!StringUtils.isEmpty(searchMap.get("keywords"))) {
            searchMap.put("keywords", searchMap.get("keywords").toString().replaceAll(" ", ""));
        }

        //查询条件  is会对关键词 分词
        Criteria criteria = new Criteria("item_title").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //按照商品分类过滤
        if (!StringUtils.isEmpty(searchMap.get("category"))) {
            //创建条件对象，is是查询的值
            Criteria categoryCriteria = new Criteria("item_category").is(searchMap.get("category"));
            //创建过滤查询对象
            SimpleFilterQuery categoryFilterQuery = new SimpleFilterQuery(categoryCriteria);
            //添加过滤条件
            query.addFilterQuery(categoryFilterQuery);
        }
        //按照商品品牌过滤
        if (!StringUtils.isEmpty(searchMap.get("brand"))) {
            //创建条件对象，is是查询的值
            Criteria brandCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            //创建过滤查询对象
            SimpleFilterQuery brandFilterQuery = new SimpleFilterQuery(brandCriteria);
            //添加过滤条件
            query.addFilterQuery(brandFilterQuery);
        }
        //按照商品规格过滤
        if (searchMap.get("spec") != null) {
            //获取每一个规格及其值
            Map<String,String> specMap = (Map<String, String>) searchMap.get("spec");
            Set<Map.Entry<String, String>> entries = specMap.entrySet();
            //遍历规格
            for (Map.Entry<String, String> entry : entries) {
                String key = entry.getKey();
                String value = entry.getValue();
                //创建条件对象
                Criteria specCriteria = new Criteria("item_spec_" + key).is(value);
                //创建过滤查询对象，参数1：域名，is之后的是查询的值
                SimpleFilterQuery specFilterQuery = new SimpleFilterQuery(specCriteria);
                //添加过滤条件
                query.addFilterQuery(specFilterQuery);
            }
        }
        //按照商品价格区间过滤
        if (!StringUtils.isEmpty(searchMap.get("price"))) {
            //创建过滤查询对象
            SimpleFilterQuery priceStartQuery = new SimpleFilterQuery();
            //将价格区间转换为价格上下限数组
            String[] prices = searchMap.get("price").toString().split("-");
            //创建下限过滤查询对象
            Criteria startCriteria = new Criteria("item_price").greaterThanEqual(prices[0]);
            priceStartQuery.addCriteria(startCriteria);
            query.addFilterQuery(priceStartQuery);

            //创建上限过滤查询对象
            if (!"*".equals(prices[1])) {
                //处理如：3000-* 时候的*
                SimpleFilterQuery priceEndQuery = new SimpleFilterQuery();
                Criteria endCriteria = new Criteria("item_price").lessThanEqual(prices[1]);
                priceEndQuery.addCriteria(endCriteria);
                query.addFilterQuery(priceEndQuery);
            }
        }

        //分页导航条
        //设置分页信息
        int pageNo = 1;
        if (searchMap.get("pageNo") != null) {
            pageNo = Integer.parseInt(searchMap.get("pageNo").toString());
        }
        int pageSize = 20;
        if (searchMap.get("pageSize") != null) {
            pageSize = Integer.parseInt(searchMap.get("pageSize").toString());
        }

        //设置起始索引号 = （页号-1）* 页大小
        query.setOffset((pageNo -1) * pageSize);
        //设置页大小
        query.setRows(pageSize);

        //设置排序
        if (!StringUtils.isEmpty(searchMap.get("sort")) && !StringUtils.isEmpty(searchMap.get("sortField"))) {
            //域名
            String sortField = searchMap.get("sortField").toString();
            //顺序，ASC/DESC
            String sortStr = searchMap.get("sort").toString();

            Sort sort = new Sort("DESC".equals(sortStr) ? Sort.Direction.DESC : Sort.Direction.ASC,"item_" + sortField);

            query.addSort(sort);
        }

        //设置高亮的配置信息
        HighlightOptions highlightOptions = new HighlightOptions();
        //高亮的域名
        highlightOptions.addField("item_title");
        //设置高亮的起始标签
        highlightOptions.setSimplePrefix("<font style='color:red'>");
        //设置高亮的结束标签
        highlightOptions.setSimplePostfix("</font>");
        query.setHighlightOptions(highlightOptions);

        //查询
//        ScoredPage<TbItem> scoredPage = solrTemplate.queryForPage(query, TbItem.class);
        HighlightPage<TbItem> highlightPage = solrTemplate.queryForHighlightPage(query, TbItem.class);

        //处理高亮标题
        List<HighlightEntry<TbItem>> highlighted = highlightPage.getHighlighted();
        if (highlighted != null && highlighted.size() > 0) {
            for (HighlightEntry<TbItem> entry : highlighted) {
                List<HighlightEntry.Highlight> highlights = entry.getHighlights();
                //获取高亮的标题
                if (highlights != null && highlights.size() > 0 && highlights.get(0).getSnipplets() != null) {
                    String title = highlights.get(0).getSnipplets().get(0);
                    //设置回商品的标题
                    entry.getEntity().setTitle(title);
                }
            }
        }

        resultMap.put("rows",highlightPage.getContent());

        //总记录数
        resultMap.put("total",highlightPage.getTotalElements());
        //总页数
        resultMap.put("totalPages",highlightPage.getTotalPages());

        return resultMap;
    }

    @Override
    public void importItemList(List<TbItem> itemList) {
        if (itemList != null && itemList.size() > 0) {
            for (TbItem tbItem : itemList) {
                Map specMap = JSON.parseObject(tbItem.getSpec(), Map.class);
                tbItem.setSpecMap(specMap);
            }

            //更新
            solrTemplate.saveBeans(itemList);
            solrTemplate.commit();
        }
    }

    @Override
    public void deleteItemByGoodsIdList(List<Long> goodsIdsList) {
        SimpleQuery query = new SimpleQuery();
        Criteria criteria = new Criteria("item_goodsid").in(goodsIdsList);

        query.addCriteria(criteria);

        solrTemplate.delete(query);
        solrTemplate.commit();
    }
}
