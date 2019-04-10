package com.pinyougou.sellergoods.service.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbBrandExample;
import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.pojo.TbSpecificationExample;
import com.pinyougou.sellergoods.service.BrandService;

import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service   //这个一定是dubbo的包,  1.他将new对象的任务交给spring框架 , 2.发布dubbo调用的服务;
@Transactional
public class BrandServiceImpl implements BrandService {
    @Autowired
    private TbBrandMapper brandMapper;

    public List<TbBrand> findAll() {
        return brandMapper.selectByExample(null);
    }

    /**
     * 通过pageHelper分页查询,返回当前页:
     * @param pageNum
     * @param pageSize
     * @return
     */
    public PageResult findPage(Integer pageNum, Integer pageSize) {
        //设置分页的起始条数和当页所需条数,源码封装到了page对象中
        PageHelper.startPage(pageNum,pageSize);
        Page<TbBrand> page = (Page<TbBrand>)brandMapper.selectByExample(null);
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 保存
     * @param tbBrand
     */
    public void save(TbBrand tbBrand) {
        brandMapper.insert(tbBrand);
    }

    /**
     * findById
     * @param id
     * @return
     */
    public TbBrand findOne(Long id) {
        return brandMapper.selectByPrimaryKey(id);
    }

    /**
     * 修改的代码:
     * @param tbBrand
     */
    public void update(TbBrand tbBrand) {
        brandMapper.updateByPrimaryKey(tbBrand);
    }

    /**
     * 批量删除:
     * @param ids
     */
    public void delete(Long[] ids) {
        for (Long id : ids) {
            //我怀疑这里边有一个事务的bug
            brandMapper.deleteByPrimaryKey(id);
        }
    }
    /*
    条件分页查询:
     */
    public PageResult search(TbBrand tbBrand, Integer pageNum, Integer pageSize) {
        //1.设置分页条件和查询条件:
        PageHelper.startPage(pageNum,pageSize);
        //创建保存条件的对象:
        TbBrandExample example = new TbBrandExample();
        //创建出条件对象:criteria = List<Criterion>
        TbBrandExample.Criteria criteria = example.createCriteria();
        //1.1调用sql 判断是否有条件查询:
        if(tbBrand != null){
            //非空,查询名称:
            String name = tbBrand.getName();
            if(name!=null && !"".equals(name)){
                //如果有条件那么应用条件查询:
                criteria.andNameLike("%"+name+"%");  //底层就是addCriterion
            }
            //获取品牌首字母
            String firstChar = tbBrand.getFirstChar();

            if(firstChar!=null && !"".equals(firstChar)){
                //页面输入了品牌首字母查询条件，组装条件查询操作
                criteria.andFirstCharEqualTo(firstChar);
            }
        }
        //exampple封装完以后,通过example条件查询:
        //2.将查询出来的list集合装换为Page
        Page<TbBrand>  page = (Page<TbBrand>)brandMapper.selectByExample(example);
        //3.返回PageResult
        PageResult result = new PageResult(page.getTotal(),page.getResult());
        return result;
    }

    /**
     * 查询数据库中所有的品牌,返回的是LIst<Map>
     * @return
     */
    @Override
    public List<Map> selectBrandOptions() {

        return brandMapper.selectBrandOptions();
    }

}
