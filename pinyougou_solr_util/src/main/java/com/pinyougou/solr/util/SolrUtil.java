package com.pinyougou.solr.util;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SolrUtil {
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    public SolrTemplate solrTemplate;

    /**
     * 将上架的商品goods.is_marketable=1和item.status为1的导入到索引库中
     */
    public void dataImport(){
        List<TbItem> itemList = itemMapper.findAllGrounding();
        //因为规格是动态的;
        //给pojo的动态域map赋值:
        for (TbItem item : itemList) {
            //将json格式的数据解析成为一个map
            Map<String,String> map = JSON.parseObject(item.getSpec(),Map.class);
            item.setDynamicSpec(map);
        }

        //将查询出来的数据导入到索引库中:
        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();
    }

}
