package com.pinyougou.page.listener;

import com.pinyougou.page.service.PageService;
import com.pinyougou.pojo.TbItem;
import freemarker.template.Configuration;
import freemarker.template.Template;
import groupEntity.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 增加静态页面:
 */
public class AddToPageListener implements MessageListener {

    @Autowired
    private FreeMarkerConfigurer freemarkerConfig;
    @Autowired
    private PageService pageService;

    public void onMessage(Message message) {
        try {
            TextMessage textMessage = (TextMessage)message;
            String goodsId = textMessage.getText();
            //1.创建configuration对象:
            Configuration configuration = freemarkerConfig.getConfiguration();
            //4.加载一个模板;创建一个模板对象;
            Template template = configuration.getTemplate("item.ftl");
            //5.创建模板使用的数据集合map
            Goods goods = pageService.findOne(Long.parseLong(goodsId));
            List<TbItem> itemList = goods.getItemList();
            for (TbItem item : itemList) {
                Map<String,Object> map = new HashMap<String,Object>();
                map.put("goods",goods);
                map.put("item",item);
                Writer out = new FileWriter("D:\\01Java\\pinyougouPages\\"+item.getId()+".html");
                //第七步：调用模板对象的process方法输出文件。
                template.process(map,out);
                //第八步：关闭流
                out.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
