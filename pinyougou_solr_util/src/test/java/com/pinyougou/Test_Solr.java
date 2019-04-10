package com.pinyougou;

import com.pinyougou.pojo.TbItem;
import com.pinyougou.solr.util.SolrUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.ScoredPage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = "classpath*:spring/applicationContext*.xml")
public class Test_Solr {

    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private SolrUtil solrUtil;

   // @Test
    public void importData(){
        solrUtil.dataImport();
    }

    /**
     *保存数据;
     */
    //@Test
    public void save(){
        TbItem item = new TbItem();
        item.setId(2L);
        item.setBrand("华为");
        item.setTitle("华为mate30 电信3G 64G  很不错");
        item.setSeller("华为旗舰店");
        solrTemplate.saveBean(item);
        solrTemplate.commit();
    }

   // @Test
    public void getById(){
        TbItem item = solrTemplate.getById(2l, TbItem.class);
        System.out.println(item.getId()+"  "+item.getBrand()+"  "+item.getTitle()+"  "+item.getSeller());
    }
   // @Test
    public void deleteById(){
        solrTemplate.deleteById("2");
        solrTemplate.commit();

    }
    /**
     * 删除所有
     */
    //@Test
    public void deleteAll(){
        Query query = new SimpleQuery("*:*");
        solrTemplate.delete(query);
        solrTemplate.commit();
    }

    /**
     * 批量保存数据:
     */
    //@Test
    public void saveBatch(){
        List<TbItem> list = new ArrayList<TbItem>();
        for (long i = 0; i < 100; i++) {
            TbItem item = new TbItem();
            item.setId(i);
            item.setBrand("华为");
            item.setTitle(i+"华为mate30 电信3G 64G  很不错");
            item.setSeller("华为"+i+"旗舰店");
            list.add(item);
        }
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }
    /**
     * 分页查询:
     */
    //@Test
    public void queryPage(){
        Query query = new SimpleQuery("*:*");
        query.setOffset(2);
        query.setRows(5);
        ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
        System.out.println("总页数-->"+page.getTotalPages());
        System.out.println("总记录数-->"+page.getTotalElements());
        //获取当前列表数据:
        List<TbItem> content = page.getContent();
        for (TbItem item : content) {
            System.out.println(item.getId()+"  "+item.getBrand()+"  "+item.getTitle()+"  "+item.getSeller());
        }
    }
    /**
     * 条件查询;
     */
    //@Test
    public void multiQuery(){
        Query query = new SimpleQuery("*:*");
        //分页条件:
        query.setOffset(0);
        query.setRows(5);
        //条件查询条件:
     Criteria criteria = new Criteria("item_title").contains("8").and("item_seller").contains("6");

        query.addCriteria(criteria);
        ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
        System.out.println("总页数-->"+page.getTotalPages());
        System.out.println("总记录数-->"+page.getTotalElements());
        //获取当前列表数据:
        List<TbItem> content = page.getContent();
        for (TbItem item : content) {
            System.out.println(item.getId()+"  "+item.getBrand()+"  "+item.getTitle()+"  "+item.getSeller());
        }
    }

}
