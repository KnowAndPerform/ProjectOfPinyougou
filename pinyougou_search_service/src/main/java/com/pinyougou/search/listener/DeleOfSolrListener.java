package com.pinyougou.search.listener;

import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.SimpleStringCriteria;
import org.springframework.data.solr.core.query.SolrDataQuery;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;

/**
 * 删除索引库中的数据;
 */
public class DeleOfSolrListener implements MessageListener{

    @Autowired
    private SolrTemplate solrTemplate;

    public void onMessage(Message message) {
        try {
            //message 就是  gooodsId

            TextMessage textMessage = (TextMessage)message;
            String goodsId = textMessage.getText();
            //item_goodsid = goodsId  的数据删除
            //先查询,在删除查询到的所有数据:

            SolrDataQuery query = new SimpleQuery("item_goodsid:"+goodsId);
            solrTemplate.delete(query);
            solrTemplate.commit();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
