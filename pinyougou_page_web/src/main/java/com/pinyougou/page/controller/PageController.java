package com.pinyougou.page.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.page.service.PageService;
import com.pinyougou.pojo.TbItem;
import freemarker.template.Configuration;
import freemarker.template.Template;
import groupEntity.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;


import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/page")
public class PageController {

    @Reference
    private PageService pageService;
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    /**
     * 商品上架的时候就生成静态页面;
     * @param goodsId
     * @return
     */
    @RequestMapping("/generateHtml")
    public String generateHtml(Long goodsId){

        try {
            //第一步：创建一个Configuration对象，直接new一个对象。构造方法的参数就是freemarker的版本号。
            Configuration configuration = freeMarkerConfigurer.getConfiguration();
            //第二步：设置模板文件所在的路径。
            //第三步：设置模板文件使用的字符集。一般就是utf-8.
            //第二,第三步已经在配置文件中做了
            //第四步：加载一个模板，创建一个模板对象。
            Template template = configuration.getTemplate("item.ftl");
            //第五步：创建一个模板使用的数据集，可以是pojo也可以是map。一般是Map。
           Goods goods =  pageService.findOne(goodsId);

            //第六步：创建一个Writer对象，一般创建一FileWriter对象，指定生成的文件名。
            List<TbItem> itemList = goods.getItemList();

            for (TbItem item : itemList) {
                Map<String , Object> map = new HashMap<String ,Object>();
                //goods.goods  goods.goodsDesc在页面自取
                map.put("goods",goods);
                //每一个item都是一个sku  所以需要生成多个静态页面:
                map.put("item",item);
                //生成html页面:
                Writer out = new FileWriter("D:\\01Java\\pinyougouPages\\"+item.getId()+".html");
                //第七步：调用模板对象的process方法输出文件。
                template.process(map,out);
                //第八步：关闭流
                out.close();
            }
            return "successful!";
        } catch (Exception e) {
            e.printStackTrace();
            return "faile!";
        }


    }
}
