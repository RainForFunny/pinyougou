package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.common.util.CookieUtils;
import com.pinyougou.pojo.TbAddress;
import com.pinyougou.pojo.TbUser;
import com.pinyougou.vo.Cart;
import com.pinyougou.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/cart")
@RestController
public class CartController {

    //品优购系统的购物车在cookie中的名称
    private static final String COOKIE_CART_LIST = "PYG_CART_LIST";
    private static final int COOKIE_CART_MAX_AGE = 60 * 60 * 24;

    @Reference
    private CartService cartService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    /**
     * 实现未登录、登录情况下将商品加入购物车
     * @param itemId 商品id
     * @param num 商品数量
     * @return 操作结果
     */
    @GetMapping("/addItemToCartList")
    @CrossOrigin(origins = "http://item.pinyougou.com",allowCredentials = "true")
    public Result addItemToCartList(Long itemId, Integer num){
//        // 设置允许跨域请求
//        response.setHeader("Access-Control-Allow-Origin", "http://item.pinyougou.com");
//        // 允许携带并接收 cookie
//        response.setHeader("Access-Control-Allow-Credentials", "true");

        Result result = Result.fail("添加失败.");
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            //获取购物车列表
            List<Cart> cartList = findCartList();
            //将商品加入购物车
            List<Cart> newCartList = cartService.addItemToCartList(cartList, itemId, num);

            //因为配置了可以匿名访问所以如果是匿名访问的时候，返回的用户名为anonymousUser
            //如果未登录则用户名为：anonymousUser
            if ("anonymousUser".equals(username)) {
                //未登录，则将数据添加到cookie中
                //设置最大有效时间为1天
                CookieUtils.setCookie(request,response,COOKIE_CART_LIST,
                        JSON.toJSONString(newCartList),COOKIE_CART_MAX_AGE,true);
            }else {
                //已登录，将数据添加到redis中
                cartService.saveCartListByUsername(newCartList,username);
            }
            result = Result.ok("添加成功。");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @GetMapping("/findCartList")
    public List<Cart> findCartList(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        //未登录,从cookie中获取购物车列表
        String cartListJSONStr = CookieUtils.getCookieValue(request, COOKIE_CART_LIST, true);
        List<Cart> cookieCartList = new ArrayList<>();
        if (!StringUtils.isEmpty(cartListJSONStr)) {
            cookieCartList = JSONArray.parseArray(cartListJSONStr, Cart.class);
        }
        //判断有没有登录
        if ("anonymousUser".equals(username)) {
            return cookieCartList;
        } else {
            //已登录
            //从redis中获取购物车列表
            List<Cart> redisCartList =  cartService.findCartListByUsername(username);
            //购物车合并，判断cookie中是否有数据
            if (cookieCartList.size() > 0) {
                //将cookie中的购物车列表与redis中的购物车列表合并到一个新的购物车列表；
                redisCartList = cartService.mergeList(redisCartList,cookieCartList);
                //将新的购物车列表保存到redis中；
                cartService.saveCartListByUsername(redisCartList,username);
                //删除cookie中的购物车；
                CookieUtils.deleteCookie(request,response,COOKIE_CART_LIST);
            }
            return redisCartList;
        }

    }


    /**
     * 获取登录用户名
     * @return 返回用户信息
     */
    @GetMapping("/getUsername")
    public Map<String,Object> getUsername(){
        Map<String,Object> map = new HashMap<>();

        //因为配置了可以匿名访问所以如果是匿名访问的时候，返回的用户名为anonymousUser
        //如果未登录则用户名为：anonymousUser
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        map.put("username",username);
        return map;
    }
}
