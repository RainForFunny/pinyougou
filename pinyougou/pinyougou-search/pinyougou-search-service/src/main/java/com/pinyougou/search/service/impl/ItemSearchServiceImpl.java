package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.core.query.result.ScoredPage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(interfaceClass =  ItemSearchService.class)
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public Map<String, Object> search(Map<String, Object> searchMap) {
        Map<String,Object> resultMap = new HashMap<>();
//        SimpleQuery query = new SimpleQuery();
        SimpleHighlightQuery query = new SimpleHighlightQuery();
        //查询条件  is会对关键词 分词
        Criteria criteria = new Criteria("item_title").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

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
        return resultMap;
    }
}
