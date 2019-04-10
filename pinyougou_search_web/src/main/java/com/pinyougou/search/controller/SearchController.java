package com.pinyougou.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.search.service.SearchService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/search")
public class SearchController {

    /**
     * 点击搜索,从solr搜索库中搜索出数据:
     * 所有的service接口都从zookeepr中查找数据
     */
    @Reference
    private SearchService searchService;
    @RequestMapping("/searchItem")
    public Map<String,Object> searchItem(@RequestBody Map<String,Object> searchMap){
        //前端传来的封装好的数据对象,格式是{key:value,key:value}
        //用map来接收
        return searchService.searchItem(searchMap);
    }
}
