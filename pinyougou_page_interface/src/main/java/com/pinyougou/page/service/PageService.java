package com.pinyougou.page.service;

import groupEntity.Goods;

public interface PageService {
    /**
     * 组装生成静态页商品数据
     */
    public Goods findOne(Long goodsId);
}
