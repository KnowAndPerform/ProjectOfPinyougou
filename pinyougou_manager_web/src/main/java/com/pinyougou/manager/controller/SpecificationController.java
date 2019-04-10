package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.sellergoods.service.SpecificationService;
import entity.PageResult;
import entity.Result;
import groupEntity.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/specification")
public class SpecificationController {

    @Reference
    private SpecificationService specificationService;

    @RequestMapping("/search")  //将前端传来的查询条件封装成一个对象,查什么封装成什么
    public PageResult search(@RequestBody TbSpecification specification ,Integer pageNum, Integer pageSize){

       PageResult result = specificationService.search(specification,pageNum,pageSize);
        return result;
    }
    @RequestMapping("/add")  //将前端传来的查询条件封装成一个对象,查什么封装成什么
    public Result add(@RequestBody Specification specification){

        try{
            specificationService.add(specification);
            return new Result(true,"新增成功!");
        }catch(Exception e){
            return new Result(false,"新增失败!");
        }
    }
    @RequestMapping("/findOne")
    public Specification findOne(Long id){
        return specificationService.findOne(id);
    }
    @RequestMapping("/update")
    public Result update(@RequestBody Specification specification){

        try{
            specificationService.update(specification);
            return new Result(true,"修改成功!");
        }catch(Exception e){
            return new Result(true,"修改失败!");
        }

    }

    /**
     * 批量删除:
     * @param ids
     * @return
     */
    @RequestMapping("/del")
    public Result del(Long[] ids){

        try{
            specificationService.delete(ids);
            return new Result(true,"删除成功!");
        }catch(Exception e){
            return new Result(true,"删除失败!");
        }

    }

    /**
     * 关联规格
     * @return
     */
    @RequestMapping("/selectSpecOptions")
    public List<Map> selectSpecOptions(){
        List<Map> options = specificationService.selectSpecificationOptions();
        return options;
    }

}
