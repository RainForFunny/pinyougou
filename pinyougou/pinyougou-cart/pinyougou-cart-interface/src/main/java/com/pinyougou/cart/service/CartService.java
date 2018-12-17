package com.pinyougou.cart.service;

import com.pinyougou.vo.Cart;

import java.util.List;

public interface CartService {
    /**
     * 添加商品到购物车中
     * @param cartList 购物车列表
     * @param itemId 商品id
     * @param num 商品数量
     */
    List<Cart> addItemToCartList(List<Cart> cartList, Long itemId, Integer num) ;

    /**
     * 根据用户名称将购物车存储对应的购物车列表
     * @param newCartList 购物车列表
     * @param username 用户名称
     */
    void saveCartListByUsername(List<Cart> newCartList, String username);

    /**
     * 根据用户名称查询购物车列表
     * @param username 用户名称
     * @return 购物车列表
     */
    List<Cart> findCartListByUsername(String username);

    /**
     * 将cookie中的购物车列表合并到redis中的购物车列表中
     * @param redisCartList redis中的购物车列表
     * @param cookieCartList cookie中的购物车列表
     * @return 合并后的redis中的购物车列表
     */
    List<Cart> mergeList(List<Cart> redisCartList, List<Cart> cookieCartList);
}
