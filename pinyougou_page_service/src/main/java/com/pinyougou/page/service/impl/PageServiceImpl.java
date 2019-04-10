package com.pinyougou.page.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.PageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import groupEntity.Goods;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PageServiceImpl implements PageService {
    /**
     * 通过goodsId查询数据库中的item goods goodsDesc  组装静态页所需要的数据;
     * @param goodsId
     * @return
     */

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbGoodsDescMapper goodsDescMapper;

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private TbItemCatMapper itemCatMapper;
    public Goods findOne(Long goodsId) {
        //查询数据库三张表,封装数据:
        Goods goods = new Goods();
        //tb_goods
        TbGoods tbGoods = goodsMapper.selectByPrimaryKey(goodsId);
        goods.setGoods(tbGoods);
        //组装页面需要展示的商品分类名称数据
        //分类封装到一个map中,在goods对象中传建一个map集合对象;
        String category1Name = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory1Id()).getName();
        String category2Name = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory2Id()).getName();
        String category3Name = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id()).getName();

        Map<String,String> categoryMap = new HashMap<String,String>();
        categoryMap.put("category1Name",category1Name);
        categoryMap.put("category2Name",category2Name);
        categoryMap.put("category3Name",category3Name);
        goods.setCategoryMap(categoryMap);

        //tb_goods_desc
        TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
        goods.setGoodsDesc(tbGoodsDesc);
        //tb_item列表数据
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andGoodsIdEqualTo(goodsId);
        List<TbItem> itemList = itemMapper.selectByExample(example);
        goods.setItemList(itemList);
        return goods;
    }
}
