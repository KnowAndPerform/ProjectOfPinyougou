package com.pinyougou.order.service.impl;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import groupEntity.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderExample;
import com.pinyougou.pojo.TbOrderExample.Criteria;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import utils.IdWorker;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;
	@Autowired
	private TbOrderItemMapper orderItemMapper;
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private IdWorker idWorker;
	@Autowired
	private TbPayLogMapper payLogMapper;


	/**
	 * 查询全部
	 */
	@Override
	public List<TbOrder> findAll() {
		return orderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbOrder> page=   (Page<TbOrder>) orderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加,在这里封装order和orderItem,每一个商家都需要一个order,但是用户可能在一个商家买很多商品,
	 * 所以需要添加每一个商品的详细订单;这里做的简单,认为购物车中的所有商品都被提交为订单了.所以		直接从redis中拿购物车数据就可以,cart
	 */
	@Override
	public void add(TbOrder order) {
		//1.订单提交之前一定需要登录,所以,直接通过用户名拿cartLISt
		List<String> ids = new ArrayList<String>();
		double totalPayment = 0.00;
		String userId = order.getUserId();
		List<Cart> cartList = (List<Cart>) redisTemplate.boundValueOps(userId).get();
		//2循环遍历购物车集合,有几个商家就有几个order
		for (Cart cart : cartList) {

			//通过cart封装order:
			TbOrder tbOrder = new TbOrder();
			long orderId = idWorker.nextId();
			tbOrder.setOrderId(orderId);
			ids.add(tbOrder.getOrderId()+"");
			//订单状态:
			tbOrder.setStatus("1");
			//订单创建时间:
			tbOrder.setCreateTime(new Date());
			//订单更新时间:
			tbOrder.setUpdateTime(new Date());
			//付款时间不在生成订单的时候封装:
			tbOrder.setUserId(userId);
			//订单来源:
			tbOrder.setSourceType("2");
			tbOrder.setSellerId(cart.getSellerId());
			//组装前端传来的数据:
			//订单的支付类型:
			tbOrder.setPaymentType(order.getPaymentType());
			//收货人区名:
			tbOrder.setReceiverAreaName(order.getReceiverAreaName());
			//收货人手机号:
			tbOrder.setReceiverMobile(order.getReceiverMobile());
			//收货人
			tbOrder.setReceiver(order.getReceiver());
			//此订单的实付金额:
			// 同时封装orderitem  订单商品明细:
			double payment =  0.00;
			List<TbOrderItem> orderItemList = cart.getOrderItemList();
			for (TbOrderItem orderItem : orderItemList) {
				payment = payment+orderItem.getTotalFee().doubleValue();
				//将orderItem封装好直接保存就好:
				orderItem.setId(idWorker.nextId());
				orderItem.setOrderId(tbOrder.getOrderId());
				orderItemMapper.insert(orderItem);
			}
			totalPayment += payment;
			tbOrder.setPayment(new BigDecimal(payment));
			//保存order
			orderMapper.insert(tbOrder);
		}
		if("1".equals(order.getPaymentType())){  //线上交易的时候才有:

			//保存完一个用户的订单的时候需生成一个总订单日志;
			TbPayLog tbPayLog = new TbPayLog();
			tbPayLog.setOutTradeNo(idWorker.nextId()+"");
			tbPayLog.setCreateTime(new Date());
			tbPayLog.setTotalFee((long)(totalPayment*100));
			tbPayLog.setUserId(userId);
			tbPayLog.setTradeState("1");  //未付款
			tbPayLog.setPayType("1");   //微信支付为1;
			//orderList  一个支付日志由所有的订单编号,组成:   1,2,3  list集合的形式:[1 ,2 ,3 ,4];
			tbPayLog.setOrderList(ids.toString().replace("[","").replace("]","").replace(" ",""));

			//保存支付日志到数据库:
			payLogMapper.insert(tbPayLog);
			//保存日志到redis缓存中;
			redisTemplate.boundHashOps("payLog").put(userId,tbPayLog);
			//保存完订单后需要删除购物车中的数据;
			redisTemplate.delete(userId);
		}
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order){
		orderMapper.updateByPrimaryKey(order);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbOrder findOne(Long id){
		return orderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			orderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbOrderExample example=new TbOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(order!=null){			
						if(order.getPaymentType()!=null && order.getPaymentType().length()>0){
				criteria.andPaymentTypeLike("%"+order.getPaymentType()+"%");
			}
			if(order.getPostFee()!=null && order.getPostFee().length()>0){
				criteria.andPostFeeLike("%"+order.getPostFee()+"%");
			}
			if(order.getStatus()!=null && order.getStatus().length()>0){
				criteria.andStatusLike("%"+order.getStatus()+"%");
			}
			if(order.getShippingName()!=null && order.getShippingName().length()>0){
				criteria.andShippingNameLike("%"+order.getShippingName()+"%");
			}
			if(order.getShippingCode()!=null && order.getShippingCode().length()>0){
				criteria.andShippingCodeLike("%"+order.getShippingCode()+"%");
			}
			if(order.getUserId()!=null && order.getUserId().length()>0){
				criteria.andUserIdLike("%"+order.getUserId()+"%");
			}
			if(order.getBuyerMessage()!=null && order.getBuyerMessage().length()>0){
				criteria.andBuyerMessageLike("%"+order.getBuyerMessage()+"%");
			}
			if(order.getBuyerNick()!=null && order.getBuyerNick().length()>0){
				criteria.andBuyerNickLike("%"+order.getBuyerNick()+"%");
			}
			if(order.getBuyerRate()!=null && order.getBuyerRate().length()>0){
				criteria.andBuyerRateLike("%"+order.getBuyerRate()+"%");
			}
			if(order.getReceiverAreaName()!=null && order.getReceiverAreaName().length()>0){
				criteria.andReceiverAreaNameLike("%"+order.getReceiverAreaName()+"%");
			}
			if(order.getReceiverMobile()!=null && order.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+order.getReceiverMobile()+"%");
			}
			if(order.getReceiverZipCode()!=null && order.getReceiverZipCode().length()>0){
				criteria.andReceiverZipCodeLike("%"+order.getReceiverZipCode()+"%");
			}
			if(order.getReceiver()!=null && order.getReceiver().length()>0){
				criteria.andReceiverLike("%"+order.getReceiver()+"%");
			}
			if(order.getInvoiceType()!=null && order.getInvoiceType().length()>0){
				criteria.andInvoiceTypeLike("%"+order.getInvoiceType()+"%");
			}
			if(order.getSourceType()!=null && order.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+order.getSourceType()+"%");
			}
			if(order.getSellerId()!=null && order.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+order.getSellerId()+"%");
			}
	
		}
		
		Page<TbOrder> page= (Page<TbOrder>)orderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
	
}
