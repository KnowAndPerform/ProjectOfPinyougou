package com.pinyougou.user.service.impl;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.pinyougou.user.service.UserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbUserMapper;
import com.pinyougou.pojo.TbUser;
import com.pinyougou.pojo.TbUserExample;
import com.pinyougou.pojo.TbUserExample.Criteria;


import entity.PageResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import utils.HttpClient;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private TbUserMapper userMapper;
	@Autowired
	private RedisTemplate redisTemplate;
	@Value("小蚁教育")
	private String signName;
	@Value("SMS_162199641")
	private String templateCode;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbUser> findAll() {
		return userMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbUser> page=   (Page<TbUser>) userMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbUser user) {
		//需要封装数据库中not null的字段;
		//1.密码需要加密:因为单点登录只能支持md5
		String password = DigestUtils.md5Hex(user.getPassword());
		user.setPassword(password);
		//2.创建时间:
		user.setCreated(new Date());
		//3.更新时间:
		user.setUpdated(new Date());
		//4.会员来源:
		user.setSourceType("1");
		//5.使用状态:
		user.setStatus("Y");
		//6.手机是否验证:
		user.setIsMobileCheck("1");
		userMapper.insert(user);
		//注册成功后,清除redis中的验证码:
		redisTemplate.delete(user.getPhone());
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbUser user){
		userMapper.updateByPrimaryKey(user);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbUser findOne(Long id){
		return userMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			userMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbUser user, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbUserExample example=new TbUserExample();
		Criteria criteria = example.createCriteria();
		
		if(user!=null){			
						if(user.getUsername()!=null && user.getUsername().length()>0){
				criteria.andUsernameLike("%"+user.getUsername()+"%");
			}
			if(user.getPassword()!=null && user.getPassword().length()>0){
				criteria.andPasswordLike("%"+user.getPassword()+"%");
			}
			if(user.getPhone()!=null && user.getPhone().length()>0){
				criteria.andPhoneLike("%"+user.getPhone()+"%");
			}
			if(user.getEmail()!=null && user.getEmail().length()>0){
				criteria.andEmailLike("%"+user.getEmail()+"%");
			}
			if(user.getSourceType()!=null && user.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+user.getSourceType()+"%");
			}
			if(user.getNickName()!=null && user.getNickName().length()>0){
				criteria.andNickNameLike("%"+user.getNickName()+"%");
			}
			if(user.getName()!=null && user.getName().length()>0){
				criteria.andNameLike("%"+user.getName()+"%");
			}
			if(user.getStatus()!=null && user.getStatus().length()>0){
				criteria.andStatusLike("%"+user.getStatus()+"%");
			}
			if(user.getHeadPic()!=null && user.getHeadPic().length()>0){
				criteria.andHeadPicLike("%"+user.getHeadPic()+"%");
			}
			if(user.getQq()!=null && user.getQq().length()>0){
				criteria.andQqLike("%"+user.getQq()+"%");
			}
			if(user.getIsMobileCheck()!=null && user.getIsMobileCheck().length()>0){
				criteria.andIsMobileCheckLike("%"+user.getIsMobileCheck()+"%");
			}
			if(user.getIsEmailCheck()!=null && user.getIsEmailCheck().length()>0){
				criteria.andIsEmailCheckLike("%"+user.getIsEmailCheck()+"%");
			}
			if(user.getSex()!=null && user.getSex().length()>0){
				criteria.andSexLike("%"+user.getSex()+"%");
			}
	
		}
		
		Page<TbUser> page= (Page<TbUser>)userMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	//发送短信的方法:需要httpClient发送url,就是调用阿里云发送短信;
	@Override
	public void sendSmsCode(String phone) throws Exception {
		//1.生成动态的6位验证码:
		int num = (int)(Math.random()+1);
		String smsCode =num + RandomStringUtils.randomNumeric(5);//因为当第一个数字是0的时候阿里云默认去掉0;
		//2.将生成的验证码保存到redis中15分钟,
		redisTemplate.boundValueOps(phone).set(smsCode,15L, TimeUnit.MINUTES);
		//3.通过httpClient调用发送短信的接口;
		//接口地址:
		HttpClient httpClient = new HttpClient("http://localhost:9999/sms/sendSms.do");
		//封装参数:
		httpClient.addParameter("phoneNumbers",phone);
		httpClient.addParameter("signName",signName);
		httpClient.addParameter("templateCode",templateCode);
		httpClient.addParameter("param","{\"code\":"+smsCode+"}");
		//发起post请求:
		httpClient.post();
		//获取响应结果:{}
		String content = httpClient.getContent();
		Map<String,String> resultMap = JSON.parseObject(content, Map.class);
		if(!resultMap.get("Code").equals("OK")){
			throw new RuntimeException("短信发送失败!");
		}
	}

    @Override
    public boolean checkCode(String phone, String smsCode) {
	    //从redis中获取smsCode
        String redisCode = (String) redisTemplate.boundValueOps(phone).get();
        if(redisCode!=null && redisCode.equals(smsCode)){
            return true;
        }
        return false;
    }

}
