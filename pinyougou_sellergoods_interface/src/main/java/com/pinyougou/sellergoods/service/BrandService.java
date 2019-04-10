package com.pinyougou.sellergoods.service;

import com.pinyougou.pojo.TbBrand;
import entity.PageResult;

import java.util.List;
import java.util.Map;

public interface BrandService {

    /**
     * 查询所有的品牌
     * @return
     */
    public List<TbBrand> findAll();

    PageResult findPage(Integer pageNum, Integer pageSize);

    void save(TbBrand tbBrand);

    TbBrand findOne(Long id);

    void update(TbBrand tbBrand);

    void delete(Long[] ids);

    PageResult search(TbBrand tbBrand, Integer pageNum, Integer pageSize);

    List<Map> selectBrandOptions();
}
