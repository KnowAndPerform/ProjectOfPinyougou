package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.sellergoods.service.BrandService;

import entity.PageResult;
import entity.Result;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/*@Controller
@ResponseBody    //保证返回给页面的是json格式:*/
@RestController  //相当于 @Controller + @ResponseBody
@RequestMapping("/brand")
public class BrandController {
    //因为是dubbo去注册中心寻找是否有服务
    @Reference
    private BrandService brandService;

    /**
     * 查询所有brand(品牌)
     * @return
     */
    @RequestMapping("/findAll")
    public List<TbBrand> findAll(){

        return brandService.findAll();
    }
    @RequestMapping("/findPage")
    public PageResult findPage(Integer pageNum, Integer pageSize){
        //因为以后可能扩展其他功能,所以不反回pageInfo
        return brandService.findPage(pageNum,pageSize);

    }
    /*
    分页条件查询:
     */
    @RequestMapping("/search")  //将前端传来的查询条件封装成一个对象,查什么封装成什么
    public PageResult search(@RequestBody TbBrand tbBrand , Integer pageNum, Integer pageSize){

        PageResult result = brandService.search(tbBrand,pageNum,pageSize);
        return result;
    }
    @RequestMapping("/add")
    public Result add(@RequestBody TbBrand tbBrand){
        Result result =null;
       try{
           brandService.save(tbBrand);
           result = new Result(true,"保存成功!");
       }catch(Exception e){
           result = new Result(true,"保存失败!");
        }
       return result;
    }
    @RequestMapping("/findOne")
    public TbBrand findOne(Long id){
        return brandService.findOne(id);
    }
    @RequestMapping("/update")
    public Result update(@RequestBody TbBrand tbBrand){

        try{
            brandService.update(tbBrand);
            return new Result(true,"修改成功!");
        }catch(Exception e){
            return new Result(true,"修改失败!");
        }

    }
    @RequestMapping("/del")
    public Result del(Long[] ids){

        try{
            brandService.delete(ids);
            return new Result(true,"删除成功!");
        }catch(Exception e){
            return new Result(true,"删除失败!");
        }

    }
    @RequestMapping("/selectBrandOptions")
    public List<Map> selectBrandOptions(){
        List<Map> options = brandService.selectBrandOptions();
        return options;
    }

}
