package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSpecificationMapper;
import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.pojo.*;
import com.pinyougou.sellergoods.service.SpecificationService;
import entity.PageResult;
import groupEntity.Specification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Service
@Controller
public class SpecificationServiceImpl implements SpecificationService {
    @Autowired
    private TbSpecificationMapper specificationMapper;
    @Autowired
    private TbSpecificationOptionMapper tbSpecificationOptionMapper;

    /**
     * 分页条件查询:
     * @param specification
     * @param pageNum
     * @param pageSize
     * @return
     */
    public PageResult search(TbSpecification specification, Integer pageNum, Integer pageSize) {
        //1.设置分页条件和查询条件:
        PageHelper.startPage(pageNum,pageSize);
        //创建保存条件的对象:
        TbSpecificationExample example = new TbSpecificationExample();
        //创建出条件对象:criteria = List<Criterion>
        TbSpecificationExample.Criteria criteria = example.createCriteria();
        //1.1调用sql 判断是否有条件查询:
        if(specification != null){
            //非空,查询名称:
            String s = specification.getSpecName();
            if(s!=null && !"".equals(s)){
                //如果有条件那么应用条件查询:
                criteria.andSpecNameLike("%"+s+"%");  //底层就是addCriterion
            }
        }
        //exampple封装完以后,通过example条件查询:
        //2.将查询出来的list集合装换为Page
        Page<TbSpecification>  page = (Page<TbSpecification>)specificationMapper.selectByExample(example);
        //3.返回PageResult
        PageResult result = new PageResult(page.getTotal(),page.getResult());
        System.out.println(result.getRows());
        return result;
    }

    /**
     * 新增的方法;
     * @param specification
     */
    public void add(Specification specification) {
        //调用数据库:
        //保存规格,会将id自动封装到tbSpecification中
        specificationMapper.insert(specification.getSpecification());
        //保存规格选项:  是数组
        List<TbSpecificationOption> options = specification.getSpecificationOptions();
        specification.getSpecification().getId();
        for (TbSpecificationOption option : options) {
            option.setSpecId(specification.getSpecification().getId());
            //调用规格的dao
            tbSpecificationOptionMapper.insert(option);
        }

    }

    /**
     * 通过id回显;
     * @param id
     * @return
     */
    public Specification findOne(Long id) {
        //首先通过id查询TbSpecification
        TbSpecification tbSpecification = specificationMapper.selectByPrimaryKey(id);
        //条件查询所有id=id:
        TbSpecificationOptionExample example = new TbSpecificationOptionExample();
        TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
        criteria.andSpecIdEqualTo(id);
        List<TbSpecificationOption> tbSpecificationOptions = tbSpecificationOptionMapper.selectByExample(example);

        return new Specification(tbSpecification,tbSpecificationOptions);
    }

    /**
     * 修改数据;
     * @param specification
     */
    public void update(Specification specification) {
        //修改TbSpecification数据:
        specificationMapper.updateByPrimaryKey(specification.getSpecification());
        //修改TbSpecificationOption数据;
        //实际上是先删除原来的所有,再一个个增加:
        TbSpecificationOptionExample example = new TbSpecificationOptionExample();
        TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
        //删除所有相等的option中的id
        criteria.andSpecIdEqualTo(specification.getSpecification().getId());
        tbSpecificationOptionMapper.deleteByExample(example);
        //删除完增加:
        List<TbSpecificationOption> options = specification.getSpecificationOptions();
        for (TbSpecificationOption option : options) {
            //关联specification的id:
            option.setSpecId(specification.getSpecification().getId());
            tbSpecificationOptionMapper.insert(option);
        }
    }

    /**
     * 批量删除:
     * @param ids
     */
    public void delete(Long[] ids) {
        for (Long id : ids) {
            //删除规格;
            specificationMapper.deleteByPrimaryKey(id);
            //删除规格选项:
            TbSpecificationOptionExample example = new TbSpecificationOptionExample();
            TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
            criteria.andSpecIdEqualTo(id);
            tbSpecificationOptionMapper.deleteByExample(example);

        }

    }

    @Override
    public List<Map> selectSpecificationOptions() {
        return specificationMapper.selectSpecificationOptions();
    }
}
