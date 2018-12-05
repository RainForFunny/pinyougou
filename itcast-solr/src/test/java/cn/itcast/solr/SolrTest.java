package cn.itcast.solr;

import com.pinyougou.pojo.TbItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.ScoredPage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/applicationContext-solr.xml")
public class SolrTest {

    @Autowired
    private SolrTemplate solrTemplate;

    /**
     * 新增或修改
     */
    @Test
    public void addOrUpdate(){
        TbItem item = new TbItem();
        item.setId(143670L);
        item.setTitle("222 JBL GO 音乐金砖 蓝牙音箱 低音炮 户外便携音响 迷你小音箱 可免提通话 魂动红");
        item.setPrice(new BigDecimal(219));
        item.setImage("https://www.yiibai.com/lucene/");
        item.setCategory("音箱");
        item.setStatus("1");

        //保存
        solrTemplate.saveBean(item);

        //提交
        solrTemplate.commit();
    }

    /**
     * 根据id删除
     */
    @Test
    public void deleteById(){
        solrTemplate.deleteById("143670");

        //提交
        solrTemplate.commit();
    }

    /**
     * 根据条件删除
     */
    @Test
    public void deleteByQuery(){
        //创建查询对象
        SimpleQuery query = new SimpleQuery();

        //设置查询条件
        Criteria criteria = new Criteria("item_title").contains("222");
        query.addCriteria(criteria);

        solrTemplate.delete(query);
        //提交
        solrTemplate.commit();
    }

    @Test
    public void deleteAll(){
        //创建查询对象
        SimpleQuery query = new SimpleQuery("*:*");

        solrTemplate.delete(query);
        //提交
        solrTemplate.commit();
    }

    /**
     * 根据分页信息查询
     */
    @Test
    public void queryForPage(){
        //创建查询对象
        SimpleQuery query = new SimpleQuery("item_title:jbl");

        //设置分页
        int page = 1;
        //起始索引号，相当于mysql limit 的起始索引
        //起始索引号=（当前页-1）*页大小
        query.setOffset(0);
        //页大小
        query.setRows(10);
        //查询
        /*
        参数1：查询对象
        参数2：查询返回结果对应的实体类（需要使用@Field）
         */
        ScoredPage<TbItem> scoredPage = solrTemplate.queryForPage(query, TbItem.class);

        showPage(scoredPage);

    }

    /**
     * 多条件查询
     */
    @Test
    public void multiQuery(){
        //创建查询对象
        SimpleQuery query = new SimpleQuery();

        //设置查询条件 contains不会分词，多条件之间属于并列的关系
        Criteria criteria = new Criteria("item_title").contains("jbl");
        Criteria criteria1 = new Criteria("item_price").lessThanEqual(300);
        query.addCriteria(criteria);
        query.addCriteria(criteria1);

        //查询
        ScoredPage<TbItem> scoredPage = solrTemplate.queryForPage(query, TbItem.class);

        showPage(scoredPage);
    }

    private void showPage(ScoredPage<TbItem> scoredPage) {
        System.out.println("总记录数：" + scoredPage.getTotalElements());
        System.out.println("总页数：" + scoredPage.getTotalPages());

        List<TbItem> itemList = scoredPage.getContent();

        for (TbItem item : itemList) {
            System.out.println("id：" + item.getId());
            System.out.println("title：" + item.getTitle());
            System.out.println("price：" + item.getPrice());
            System.out.println("image：" + item.getImage());
            System.out.println("category：" + item.getCategory());
            System.out.println("status：" + item.getStatus());

        }
    }
}
