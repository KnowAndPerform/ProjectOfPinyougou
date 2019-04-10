package groupEntity;

import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Goods implements Serializable {
    //spu
    private TbGoods goods;
    private TbGoodsDesc goodsDesc;
    //sku
    private List<TbItem> itemList;  //因为一个spu 对应多个sku 具体的飞了,items是条款,项目的意思.

    private Map<String , String> categoryMap;  //这个用在详情的静态页面的面包屑中.

    public Goods() {
    }

    public Goods(TbGoods goods, TbGoodsDesc goodsDesc, List<TbItem> itemList, Map<String, String> categoryMap) {
        this.goods = goods;
        this.goodsDesc = goodsDesc;
        this.itemList = itemList;
        this.categoryMap = categoryMap;
    }

    @Override
    public String toString() {
        return "Goods{" +
                "goods=" + goods +
                ", goodsDesc=" + goodsDesc +
                ", itemList=" + itemList +
                ", categoryMap=" + categoryMap +
                '}';
    }

    public TbGoods getGoods() {
        return goods;
    }

    public void setGoods(TbGoods goods) {
        this.goods = goods;
    }

    public TbGoodsDesc getGoodsDesc() {
        return goodsDesc;
    }

    public void setGoodsDesc(TbGoodsDesc goodsDesc) {
        this.goodsDesc = goodsDesc;
    }

    public List<TbItem> getItemList() {
        return itemList;
    }

    public void setItemList(List<TbItem> itemList) {
        this.itemList = itemList;
    }

    public Map<String, String> getCategoryMap() {
        return categoryMap;
    }

    public void setCategoryMap(Map<String, String> categoryMap) {
        this.categoryMap = categoryMap;
    }
}
