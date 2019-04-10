package com.pinyougou.page.listener;

import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.io.File;
import java.util.List;

/**
 * 删除静态页;
 */
public class DeleOfPageListener implements MessageListener {

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;
    @Autowired
    private TbItemMapper tbItemMapper;
    public void onMessage(Message message) {

        try {
            TextMessage textMessage = (TextMessage) message;
            String goodsId = textMessage.getText();

            //查询下架商品的所有item
            TbItemExample example = new TbItemExample();
            TbItemExample.Criteria criteria = example.createCriteria();
            criteria.andGoodsIdEqualTo(Long.parseLong(goodsId));
            List<TbItem> itemList =  tbItemMapper.selectByExample(example);
            //遍历itemList   将所有的item.id删除:
            for (TbItem item : itemList) {
                new File("D:\\01Java\\pinyougouPages\\"+item.getId()+".html").delete();
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

}
