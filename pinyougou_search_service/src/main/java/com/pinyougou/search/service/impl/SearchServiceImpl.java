package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.core.query.result.ScoredPage;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class SearchServiceImpl implements SearchService {

    @Autowired
    private SolrTemplate solrTemplate;
    /**
     * 根据条件搜索:
     * @param searchMap
     * @return
     */
    public Map<String, Object> searchItem(Map searchMap) {
        Map<String , Object> resultMap = new HashMap<String, Object>();
        //创建高亮查询对象:
        HighlightQuery query = new SimpleHighlightQuery();
        //创建条件对象:
        Criteria criteria = null;
        //1.查询{key:value,key:value}中的keywords关键字:
        String keywords = (String) searchMap.get("keywords");
        if(keywords != null && !"".equals(keywords)){
            //通过关键字查询solr索引库中的数据:
            criteria = new Criteria("item_title").is(keywords);

        }else{
           criteria =  new Criteria().expression("*:*");
        }
        //将查询条件对象:criteria --> 查询对象:query
        query.addCriteria(criteria);

        // 2.添加分类过滤条件:
        String category = (String) searchMap.get("category");
        if(category !=null && !"".equals(category)){
            //创建一个局部的过滤条件对象:
            Criteria filterCriteria = new Criteria("item_category").is(category);
            FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
            //将过滤条件查询添加到query中去:
            query.addFilterQuery(filterQuery);
        }

        // 3.添加品牌过滤条件:
        String brand = (String) searchMap.get("brand");
        if(brand !=null && !"".equals(brand)){
            //创建一个局部的过滤条件对象:
            Criteria filterCriteria = new Criteria("item_brand").is(brand);
            FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
            //将过滤条件查询添加到query中去:
            query.addFilterQuery(filterQuery);
        }

        //4.添加一个spec过滤条件:
        Map<String,String> specMap = (Map<String, String>) searchMap.get("spec");
        if(specMap != null){
            //遍历map集合:(你要记得,我们是要去solr的索引库中查询数据:)
            for(String key : specMap.keySet()){
                Criteria filterCriteria = new Criteria("item_spec_"+key).is(specMap.get(key));
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }

        //5.价格过滤:
        String priceRange = (String) searchMap.get("price");
        if(priceRange != null && !"".equals(priceRange)){
            //获取价格:0-1000,1000-1500,3000+
            String[] price = priceRange.split("-");
            //首先设定起始价格:一定是spring[0]
            Criteria minPrice = new Criteria("item_price").greaterThan(price[0]);
            //过滤最小值;
            FilterQuery filterMin = new SimpleFilterQuery(minPrice);
            query.addFilterQuery(filterMin);
            //判断有没有最大值,有过滤,没有不管:
            if(!"*".equals(price[1])){
                //有上限:<=
                Criteria maxPrice = new Criteria("item_price").lessThanEqual(price[1]);
                FilterQuery filterMax = new SimpleFilterQuery(maxPrice);
                query.addFilterQuery(filterMax);
            }
        }

        //6.设置排序方式和排序字段:
        String sortField = (String) searchMap.get("sortField");
        String sort = (String) searchMap.get("sort");
        if(sortField!=null && !"".equals(sortField)){
            //升序:
            if("ASC".equals(sort)){
                query.addSort(new Sort(Sort.Direction.ASC,"item_"+sortField));
            }
            if("DESC".equals(sort)){
                query.addSort(new Sort(Sort.Direction.DESC,"item_"+sortField));
            }
        }

        //7.设置分页查询条件:
        Integer pageNo = (Integer) searchMap.get("pageNo");
        Integer pageSize = (Integer) searchMap.get("pageSize");
        if(pageNo == null){
            pageNo = 1;  //默认第一页
        }
        if(pageSize == null){
            pageSize = 20;  //默认每页显示20条
        }
        query.setOffset((pageNo-1)*pageSize);
        query.setRows(pageSize);

        //设置高亮对象:
        HighlightOptions highlightOptions = new HighlightOptions();
        //设置需要高亮的字段:
        highlightOptions.addField("item_title");
        //添加高亮字的标签:
        highlightOptions.setSimplePrefix("<font color='red'>");
        highlightOptions.setSimplePostfix("</font>");
        //将高亮关联到query中
        query.setHighlightOptions(highlightOptions);
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
        //替换高亮数据到content中的item.title
        List<HighlightEntry<TbItem>> highlighted = page.getHighlighted();
        //循环高亮数据:
        for (HighlightEntry<TbItem> highlightEntry : highlighted) {
            TbItem item = highlightEntry.getEntity();
            if(highlightEntry.getHighlights().size()>0 &&
                    highlightEntry.getHighlights().get(0).getSnipplets().size()>0 &&
                    keywords.length()>0){
                //当有数据的时候给title设置为有标签的数据:
                item.setTitle(highlightEntry.getHighlights().get(0).getSnipplets().get(0));
            }
        }
        //前端页面:{"rows":items,}
        resultMap.put("rows",page.getContent());
        resultMap.put("totalPages",page.getTotalPages());
        resultMap.put("pageNo",pageNo);
        return resultMap;
    }

}
