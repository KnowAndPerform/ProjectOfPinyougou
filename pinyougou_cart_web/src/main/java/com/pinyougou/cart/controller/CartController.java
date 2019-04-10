package com.pinyougou.cart.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.cart.service.CartService;
import entity.Result;
import groupEntity.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import utils.CookieUtil;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference
    private CartService cartService;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;
    @Autowired
    private HttpSession session;
    /**
     * 首先,保存商品到购物车:
     */
    @RequestMapping("/addItemToCartList")
    @CrossOrigin(origins = "http://item.pinyougou.com",allowCredentials = "true")
    public Result addItemToCartList(String itemId,Integer num){

        try {
            //首先向购物车中存数据:
            //1.先从redis中取出cartList
            List<Cart> cartList = findCartList();
            //将商品保存到购物车:
            cartList = cartService.addItemToCartList(cartList,itemId,num);
            //以下皆是保存购物车集合到redis中的过程;
            //用户没有登录的时候用sessionID为key保存到redis中,用户登录用username保存;
            //1.判断username是否为空:
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            if("anonymousUser".equals(username)){
                //未登录,通过sessionId保存
                String sessionId = getSessionId();
                cartService.saveCartListToRedisBySessionId(sessionId,cartList);
            }else{
                //已登录,通过usernmae合并保存:
                cartService.saveCartListToRedisByUsername(username,cartList);
            }
            return new Result(true,"添加购物车成功!");
        }catch(RuntimeException re){
            re.printStackTrace();
            return new Result(false,re.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加购物车失败!");
        }
    }

    /**
     * 从redis中获取购物车;
     * @return
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList(){
        //1.判断username是否为空:
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

            //获取sessionID,通过sessionID获取redis中的cartList;
            String sessionId = getSessionId();
            List<Cart> cartListBySessionId = cartService.findCartListByKey(sessionId);

        if(username.equals("anonymousUser")){
            return cartListBySessionId;
        }else{

            //如果username存在,通过username查询:
            List<Cart> cartListByUsername = cartService.findCartListByKey(username);
            //判断登陆之前是否添加过购物车:
            if(cartListBySessionId!=null){
                //添加过需要将通过sessionId添加的商品和通过用户名查到的商品合并;
             cartListByUsername = cartService.mergeCartList(cartListBySessionId,cartListByUsername);
                //合并成功后将缓存中的数据清除;
                cartService.deleteRedisBySessionId(sessionId);
                cartService.saveCartListToRedisByUsername(username,cartListByUsername);
            }
            //因为数据传输的时候fastJson解析不了null;所以需要返回一个对象;
            return cartListByUsername;
        }
    }

    /**
     * 如果是第一次请求
     * 需要创建一个cookieName:cartCookie    cookieValue:sessionID的cookie,并且声明周期是7天;
     * @return
     */
    private String getSessionId(){
        //通过CookieUtil获得sessionID,没有cartCookie就创建一个;返回给客户端/浏览器
        String sessionId = CookieUtil.getCookieValue(request,"cartCookie","utf-8");
        if(sessionId==null){
            //你要记得,只要访问到这个方法了,证明访问了servlet,所以一定会为此次访问创建一个session对象,所以可以从session中获取他的id值 ,这个id值是服务器自己创建的不会重复,所以放心使用吧!;
           String sessionId1 = request.getSession().getId();
           //看看这两个session是不是一个,估计一定是;
            sessionId = session.getId();
            //这个方法默认cookie的maxAge=-1;一次会话;ookieUtil.setCookie(request,response,"cartCookie",sessionId);
            //没有名字为cartCookie的cookie,也就是此一次访问服务器,所以需要创建一个cookie
            CookieUtil.setCookie(request,response,"cartCookie",sessionId,3600*24*7,"utf-8");
        }
        return sessionId;
    }
}
