package com.pinyougou.search.listener;

import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;

/**
 * 向索引库中添加数据;
 */
public class AddToSolrListener implements MessageListener {

    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private TbItemMapper tbItemMapper;

    public void onMessage(Message message) {
        try {
                //message 就是  gooodsId
                //通过id查询数据库:向solr索引库中添加查询到的数据:
                TextMessage textMessage = (TextMessage)message;
                String goodsId = textMessage.getText();
                TbItemExample example = new TbItemExample();
                TbItemExample.Criteria criteria = example.createCriteria();
                criteria.andGoodsIdEqualTo(Long.parseLong(goodsId));
                List<TbItem> items = tbItemMapper.selectByExample(example);
                //将list集合同步到索引库;
                solrTemplate.saveBeans(items);
                 solrTemplate.commit();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
