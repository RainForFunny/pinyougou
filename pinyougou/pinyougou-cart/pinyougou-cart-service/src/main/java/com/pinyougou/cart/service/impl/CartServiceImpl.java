package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.ItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.vo.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service(interfaceClass = CartService.class)
public class CartServiceImpl implements CartService {

    //购物车列表在redis中的key
    private static final String REDIS_CART_LIST = "REDIS_CART_LIST";

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 添加商品到购物车中
     *
     * @param cartList 购物车列表
     * @param itemId   商品id
     * @param num      商品数量
     */
    @Override
    public List<Cart> addItemToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //1. 根据商品sku id查询商品并判断是否是否存在与已启用；
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if (item == null) {
            throw new RuntimeException("商品不存在。");
        }
        if (!"1".equals(item.getStatus())) {
            throw new RuntimeException("商品状态非法。");
        }
        //2. 判断商品对应的商家cart是否存在在购物车列表
        Cart cart = findCartBySellerId(cartList,item.getSellerId());
        if (cart == null) {
            if (num > 0) {
                //3. 商家不存在；则直接先添加一个商家cart，在该商家的商品列表中加入商品
                cart = new Cart();
                cart.setSellerId(item.getSellerId());
                cart.setSellerName(item.getSeller());

                List<TbOrderItem> orderItemList = new ArrayList<>();
                //创建订单商品
                TbOrderItem orderItem = createOrderItem(item,num);
                orderItemList.add(orderItem);

                cart.setOrderItemList(orderItemList);
                cartList.add(cart);
            } else {
                throw new RuntimeException("购买数量非法");
            }
        } else {
            //4. 商家存在
            TbOrderItem tbOrderItem = findOrderItemByItemId(cart.getOrderItemList(),itemId);
            if (tbOrderItem == null) {
                //5. 在商家中商品如果不存在；将商品直接加入该商家的商品列表
                if (num > 0) {
                    tbOrderItem = createOrderItem(item,num);
                    cart.getOrderItemList().add(tbOrderItem);
                } else {
                    throw new RuntimeException("购买数量非法");
                }
            } else {
                //6. 在商家中商品如果存在；则将商品列表中对应的商品购买数量叠加。
                tbOrderItem.setNum(tbOrderItem.getNum() + num);
                //总金额
                tbOrderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue() * tbOrderItem.getNum()));
                // 叠加之后购买数量为0则需要将该订单商品从订单商品列表中移除，
                if (tbOrderItem.getNum() < 1) {
                    cart.getOrderItemList().remove(tbOrderItem);
                }
                // 如果移除之后商品列表为空则需要将该商家（cart）从购物车列表中移除
                if (cart.getOrderItemList().size() == 0) {
                    cartList.remove(cart);
                }
            }
        }
        return cartList;
    }

    @Override
    public void saveCartListByUsername(List<Cart> newCartList, String username) {
        redisTemplate.boundHashOps(REDIS_CART_LIST).put(username,newCartList);
    }

    @Override
    public List<Cart> findCartListByUsername(String username) {
        List<Cart> redisCartList = (List<Cart>) redisTemplate.boundHashOps(REDIS_CART_LIST).get(username);
        if (redisCartList != null) {
            return redisCartList;
        }
        return new ArrayList<>();
    }

    @Override
    public List<Cart> mergeList(List<Cart> redisCartList, List<Cart> cookieCartList) {
        for (Cart cart : cookieCartList) {
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                addItemToCartList(redisCartList,orderItem.getItemId(),orderItem.getNum());
            }
        }
        return redisCartList;
    }

    /**
     * 根据商品id从订单商品列表中查询订单商品
     * @param orderItemList 订单商品列表
     * @param itemId 商品id
     * @return 订单商品
     */
    private TbOrderItem findOrderItemByItemId(List<TbOrderItem> orderItemList, Long itemId) {
        if (orderItemList != null && orderItemList.size() > 0 ) {
            for (TbOrderItem orderItem : orderItemList) {
                if (itemId.equals(orderItem.getItemId())) {
                    return orderItem;
                }
            }
        }
        return null;
    }

    private TbOrderItem createOrderItem(TbItem item, Integer num) {
        TbOrderItem orderItem = new TbOrderItem();

        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTitle(item.getTitle());
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue() * num));

        return orderItem;
    }

    /**
     * 从购物车列表中查询购物车对象cart
     * @param cartList 购物车列表
     * @param sellerId 商家id
     * @return 购物车对象
     */
    private Cart findCartBySellerId(List<Cart> cartList, String sellerId) {
        if (cartList != null && cartList.size() > 0) {
            for (Cart cart : cartList) {
                if (sellerId.equals(cart.getSellerId())) {
                    return cart;
                }
            }
        }
        return null;
    }
}
