package com.pinyougou.content.service.impl;
import java.util.List;

import com.pinyougou.content.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbContentMapper;
import com.pinyougou.pojo.TbContent;
import com.pinyougou.pojo.TbContentExample;
import com.pinyougou.pojo.TbContentExample.Criteria;


import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private TbContentMapper contentMapper;
	@Autowired
	private RedisTemplate redisTemplate;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbContent> findAll() {
		return contentMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbContent> page=   (Page<TbContent>) contentMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbContent content) {
		//新增广告的时候需要将该类下的redis清空:
		contentMapper.insert(content);
		redisTemplate.boundHashOps("content").delete(content.getCategoryId());
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbContent content){
		//判断,如果修改了广告的分类,那么需要删除原两个分类广告的缓存,如果只修改内容,删除本分类缓存就可以;
		//先通过content id 查询数据库;
		TbContent tbContent = contentMapper.selectByPrimaryKey(content.getId());
		Long oldCategoryId = tbContent.getCategoryId();
		//老的必删除:
		redisTemplate.boundHashOps("content").delete(oldCategoryId);

		if(oldCategoryId.longValue()!=content.getCategoryId().longValue()){
			//再删除新的id;
			redisTemplate.boundHashOps("content").delete(content.getCategoryId());
		}
		contentMapper.updateByPrimaryKey(content);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbContent findOne(Long id){
		return contentMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			//删除广告的时候需要将原来的数据从缓存中删除:
			TbContent tbContent = contentMapper.selectByPrimaryKey(id);
			redisTemplate.boundHashOps("content").delete(tbContent.getCategoryId());
			contentMapper.deleteByPrimaryKey(id);
		}
	}
	
	
		@Override
	public PageResult findPage(TbContent content, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbContentExample example=new TbContentExample();
		Criteria criteria = example.createCriteria();
		
		if(content!=null){			
						if(content.getTitle()!=null && content.getTitle().length()>0){
				criteria.andTitleLike("%"+content.getTitle()+"%");
			}
			if(content.getUrl()!=null && content.getUrl().length()>0){
				criteria.andUrlLike("%"+content.getUrl()+"%");
			}
			if(content.getPic()!=null && content.getPic().length()>0){
				criteria.andPicLike("%"+content.getPic()+"%");
			}
			if(content.getStatus()!=null && content.getStatus().length()>0){
				criteria.andStatusLike("%"+content.getStatus()+"%");
			}
	
		}
		
		Page<TbContent> page= (Page<TbContent>)contentMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 通过类别id 查询该类下的所有广告
	 * @param cid
	 * @return
	 */
	@Override
	public List<TbContent> findContentsByCategoryId(Long cid) {

		//首先从redis中查询,如果没有查询到,再通过数据库查询,查询后需要存到redis中,以便下次查询:
		List<TbContent> list  = (List<TbContent>)redisTemplate.boundHashOps("content").get("cid");
		if(list == null ){   //数据库查询:
			System.out.println("from mysql 数据库!...........*************");
			TbContentExample example = new TbContentExample();
			Criteria criteria = example.createCriteria();
			//条件1.cid
			criteria.andCategoryIdEqualTo(cid);
			//条件2.必须是有效状态
			criteria.andStatusEqualTo("1");
			//条件3.根据给价排序:  因为sql语句中是根据某字段排序的,所以是数据库中的字段名称
			example.setOrderByClause("sort_order");
			list = contentMapper.selectByExample(example);
			//将查询结果放到redis
			redisTemplate.boundHashOps("content").put("cid",list);
			return list;
		}else{
			System.out.println("from redis ..........***************");
			return list;
		}

	}

}
