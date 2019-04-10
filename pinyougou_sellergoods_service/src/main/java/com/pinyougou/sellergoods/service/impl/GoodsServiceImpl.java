package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbGoodsExample;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.sellergoods.service.GoodsService;
import entity.PageResult;
import groupEntity.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.alibaba.fastjson.JSON.parseArray;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private TbGoodsMapper goodsMapper;
    @Autowired
    private TbGoodsDescMapper goodsDescMapper;
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private TbItemCatMapper itemCatMapper;
    @Autowired
    private TbBrandMapper brandMapper;
    @Autowired
    private TbSellerMapper sellerMapper;
    //用来发布消息:
    @Autowired
    private JmsTemplate jmsTemplate;
    //商品上架的时候,增加itemtosolr的队列
    @Autowired
    private Destination addItemToSolrDestination;
    //商品下架的时候,删除itemofsolr的队列
    @Autowired
    private Destination deleItemOfSolrDestination;
    //早呢更加和删除静态页;
    @Autowired
    private Destination addItemToPageDestination;
    @Autowired
    private Destination deleItemOfPageDestination;



    /**
     * 查询全部
     */
    @Override
    public List<TbGoods> findAll() {
        return goodsMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(Goods goods) {
        //首先保存goods的数据:
        TbGoods tbGoods = goods.getGoods();
        tbGoods.setAuditStatus("0");//audit_status
        goodsMapper.insert(tbGoods);
        //2保存tbGoodsDesc
        TbGoodsDesc tbGoodsDesc = goods.getGoodsDesc();
        //产品描述的主键是goods的id  ,所以必须赋值给他.
        tbGoodsDesc.setGoodsId(tbGoods.getId());
        goodsDescMapper.insert(tbGoodsDesc);

        if (tbGoods.getIsEnableSpec().equals("1")) {
            //保存items
            List<TbItem> items = goods.getItemList();
            for (TbItem item : items) {
                //`title` varchar(100) NOT NULL COMMENT '商品标题'  // 商品名称（SPU名称）+ 商品规格选项名称 中间以空格隔开
                String title = tbGoods.getGoodsName();
                //{"spec":{"网络":"移动3G","机身内存":"16G"},取第一条数据
                String spec = item.getSpec();
                Map<String, String> map = JSON.parseObject(spec, Map.class);
                for (String key : map.keySet()) {
                    title += " " + map.get(key);
                }
                item.setTitle(title);
                setItemValue(tbGoods, tbGoodsDesc, item);

                itemMapper.insert(item);
            }
        }else{//不启用规格,要自己封装规格内的数据:
            TbItem item = new TbItem();
            //手动封装title
            item.setTitle(tbGoods.getGoodsName());
            setItemValue(tbGoods, tbGoodsDesc, item);
        //     `spec` varchar(200) DEFAULT NULL,
            item.setSpec("{}");  //没有规格选项也就没有规格
        //	 `price` decimal(20,2) NOT NULL COMMENT '商品价格，单位为：元',
            item.setPrice(tbGoods.getPrice());
        //	 `num` int(10) NOT NULL COMMENT '库存数量',
            item.setNum(999);  //秒杀不能超出库存数量
        //	 `status` varchar(1) NOT NULL COMMENT '商品状态，1-正常，2-下架，3-删除',
            item.setStatus("1");
        //	 `is_default` varchar(1) DEFAULT NULL,
            item.setIsDefault("1");
            itemMapper.insert(item);
        }


    }

    private void setItemValue(TbGoods tbGoods, TbGoodsDesc tbGoodsDesc, TbItem item) {
        //	  `image` varchar(2000) DEFAULT NULL COMMENT '商品图片',  // 从 tb_goods_desc item_images中获取第一张
        //[{"color":"红色","url":"http://192.168.25.133/group1/M00/00/01/wKgZhVmHINKADo__AAjlKdWCzvg874.jpg"},
        String itemImages = tbGoodsDesc.getItemImages();
        if (itemImages != null && itemImages.length() > 0) {
            List<Map> imageList = JSON.parseArray(itemImages, Map.class);
            String url = (String) imageList.get(0).get("url");
            item.setImage(url);
        }
        //	  `categoryId` bigint(10) NOT NULL COMMENT '所属类目，叶子类目',  //三级分类id
        item.setCategoryid(tbGoods.getCategory3Id());
        //	  `create_time` datetime NOT NULL COMMENT '创建时间',
        item.setCreateTime(new Date());
        //	  `update_time` datetime NOT NULL COMMENT '更新时间',
        item.setUpdateTime(new Date());
        //	  `goods_id` bigint(20) DEFAULT NULL,
        item.setGoodsId(tbGoods.getId());
        //	  `seller_id` varchar(30) DEFAULT NULL,
        item.setSellerId(tbGoods.getSellerId());
        //			//以下字段作用：方便商品搜索
        //	  `category` varchar(200) DEFAULT NULL, //三级分类名称
        item.setCategory(itemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id()).getName());
        //	  `brand` varchar(100) DEFAULT NULL,//品牌名称
        item.setBrand(brandMapper.selectByPrimaryKey(tbGoods.getBrandId()).getName());
        //	  `seller` varchar(200) DEFAULT NULL,//商家店铺名称
        item.setSeller(sellerMapper.selectByPrimaryKey(tbGoods.getSellerId()).getNickName());
        //将封装好的item数据保存到数据库:
    }


    /**
     * 修改
     */
    @Override
    public void update(TbGoods goods) {
        goodsMapper.updateByPrimaryKey(goods);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbGoods findOne(Long id) {
        return goodsMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            goodsMapper.deleteByPrimaryKey(id);
        }
    }


    @Override
    public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbGoodsExample example = new TbGoodsExample();
        Criteria criteria = example.createCriteria();

        if (goods != null) {
            if (goods.getSellerId() != null && goods.getSellerId().length() > 0) {
                //这里必须是精准查询:
                criteria.andSellerIdEqualTo(goods.getSellerId());
            }
            if (goods.getGoodsName() != null && goods.getGoodsName().length() > 0) {
                criteria.andGoodsNameLike("%" + goods.getGoodsName() + "%");
            }
            if (goods.getAuditStatus() != null && goods.getAuditStatus().length() > 0) {
                criteria.andAuditStatusEqualTo( goods.getAuditStatus());
            }
            if (goods.getIsMarketable() != null && goods.getIsMarketable().length() > 0) {
                criteria.andIsMarketableLike("%" + goods.getIsMarketable() + "%");
            }
            if (goods.getCaption() != null && goods.getCaption().length() > 0) {
                criteria.andCaptionLike("%" + goods.getCaption() + "%");
            }
            if (goods.getSmallPic() != null && goods.getSmallPic().length() > 0) {
                criteria.andSmallPicLike("%" + goods.getSmallPic() + "%");
            }
            if (goods.getIsEnableSpec() != null && goods.getIsEnableSpec().length() > 0) {
                criteria.andIsEnableSpecLike("%" + goods.getIsEnableSpec() + "%");
            }
            if (goods.getIsDelete() != null && goods.getIsDelete().length() > 0) {
                criteria.andIsDeleteLike("%" + goods.getIsDelete() + "%");
            }

        }

        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void updateStatus(Long[] ids, String status) {
        //查询数据库,修改goods 的status
        for (Long id : ids) {
            //查
            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
            tbGoods.setAuditStatus(status);
            //改
            goodsMapper.updateByPrimaryKey(tbGoods);
        }
    }

    @Override
    public void updateIsMarketable(Long[] ids, String isMarketable) {
        for (final Long id : ids) {
            //先查询:
             TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
            //只有商品审核通过的才能上下架:
            if(tbGoods.getAuditStatus().equals("1")){
                tbGoods.setIsMarketable(isMarketable);
                //修改数据库中的数据;
                goodsMapper.updateByPrimaryKey(tbGoods);
                /*@Autowired
                private Destination addItemToSolrDestination;
                //商品下架的时候,删除itemofsolr的队列
                @Autowired
                private Destination deleItemOfSolrDestination;
                //早呢更加和删除静态页;
                @Autowired
                private Destination addItemToPageDestination;
                @Autowired
                private Destination deleItemOfPageDestination;*/
                //商品上架的时候:增加solr和静态页:
                if(isMarketable.equals("1")){
                    jmsTemplate.send(addItemToSolrDestination, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            return session.createTextMessage(id+"");
                        }
                    });
                    jmsTemplate.send(addItemToPageDestination, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            return session.createTextMessage(id+"");
                        }
                    });
                }
               /* //商品上架的时候:增加solr和静态页:
                if(isMarketable.equals("1")){
                    jmsTemplate.send(addItemToPageDestination, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            return session.createTextMessage(id+"");
                        }
                    });
                }*/
                //商品下架时候:删除solr和静态页:
                if(isMarketable.equals("0")){
                    jmsTemplate.send(deleItemOfSolrDestination, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            return session.createTextMessage(id+"");
                        }
                    });
                    jmsTemplate.send(deleItemOfPageDestination, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            return session.createTextMessage(id+"");
                        }
                    });
                }
                /*//商品下架时候:删除solr和静态页:
                if(isMarketable.equals("0")){
                    jmsTemplate.send(deleItemOfPageDestination, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            return session.createTextMessage(id+"");
                        }
                    });
                }*/

            }else{
                throw new RuntimeException("审核通过的商品才能上下架!");
            }

        }
    }

}
