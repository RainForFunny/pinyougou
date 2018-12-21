package com.pinyougou.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.mapper.OrderItemMapper;
import com.pinyougou.mapper.OrderMapper;
import com.pinyougou.mapper.PayLogMapper;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.service.impl.BaseServiceImpl;
import com.pinyougou.vo.Cart;
import com.pinyougou.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service(interfaceClass = OrderService.class)
public class OrderServiceImpl extends BaseServiceImpl<TbOrder> implements OrderService {

    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private PayLogMapper payLogMapper;

    @Autowired
    private IdWorker idWorker;

    //购物车列表在redis中的key
    private static final String REDIS_CART_LIST = "REDIS_CART_LIST";

    @Override
    public PageResult search(Integer page, Integer rows, TbOrder order) {
        PageHelper.startPage(page, rows);

        Example example = new Example(TbOrder.class);
        Example.Criteria criteria = example.createCriteria();
        /*if(!StringUtils.isEmpty(order.get***())){
            criteria.andLike("***", "%" + order.get***() + "%");
        }*/

        List<TbOrder> list = orderMapper.selectByExample(example);
        PageInfo<TbOrder> pageInfo = new PageInfo<>(list);

        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public String addOrder(TbOrder order) {
        //定义支付订单号
        String outTradeNo = "";
        //1. 获取redis中的购物车列表
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps(REDIS_CART_LIST).get(order.getUserId());
        if (cartList != null && cartList.size() > 0) {
            //本次订单的总金额
            Double totalPayment = 0.0;
            //订单的id集合
            String orderIds = "";
            //2. 遍历购物车列表，每个购物车对象Cart对应一个订单
            for (Cart cart : cartList) {
                TbOrder tbOrder = new TbOrder();
                tbOrder.setOrderId(idWorker.nextId());
                tbOrder.setSourceType(order.getSourceType());
                tbOrder.setPaymentType(order.getPaymentType());
                tbOrder.setUserId(order.getUserId());
                tbOrder.setCreateTime(new Date());
                //未支付 0
                tbOrder.setStatus("0");
                tbOrder.setReceiver(order.getReceiver());
                tbOrder.setReceiverAreaName(order.getReceiverAreaName());
                tbOrder.setReceiverMobile(order.getReceiverMobile());
                tbOrder.setUpdateTime(tbOrder.getCreateTime());
                tbOrder.setSellerId(cart.getSellerId());

                //当前订单的总金额= 当前订单的所有格订单明细金额之和
                Double payment = 0.0;

                //3. 遍历购物车对象Cart中订单明细列表一个个的保存到订单明细表tb_order_item
                for (TbOrderItem orderItem : cart.getOrderItemList()) {
                    orderItem.setId(idWorker.nextId());
                    orderItem.setOrderId(tbOrder.getOrderId());

                    //累积当前订单的总金额
                    payment += orderItem.getTotalFee().doubleValue();

                    //保存订单明细
                    orderItemMapper.insertSelective(orderItem);
                }

                //当前订单支付总金额
                tbOrder.setPayment(new BigDecimal(payment));

                //本次交易总金额
                totalPayment += payment;

                if (orderIds.length() > 0) {
                    orderIds += "," + tbOrder.getOrderId();
                } else {
                    orderIds = tbOrder.getOrderId().toString();
                }

                orderMapper.insertSelective(tbOrder);
            }

            if ("1".equals(order.getPaymentType())) {
                //4. 如果为微信支付的话生成支付日志信息保存到tb_pay_log
                TbPayLog payLog = new TbPayLog();
                payLog.setUserId(order.getUserId());
                payLog.setCreateTime(new Date());
                outTradeNo = idWorker.nextId() + "";
                payLog.setOutTradeNo(outTradeNo);
                //1  未支付
                payLog.setTradeState("1");
                payLog.setPayTime(new Date());
                payLog.setTotalFee((long) (totalPayment * 100));
                payLog.setOrderList(orderIds);

                payLogMapper.insertSelective(payLog);
            }
        }
        //5. 将redis中该用户对应的购物车数据删除
        redisTemplate.boundHashOps(REDIS_CART_LIST).delete(order.getUserId());
        //6. 返回支付日志id
        return outTradeNo;
    }

    @Override
    public TbPayLog findPayLogById(String outTradeNo) {
        return payLogMapper.selectByPrimaryKey(outTradeNo);
    }

    @Override
    public void updateOrderStatus(String outTradeNo, String transaction_id) {
        TbPayLog payLog = findPayLogById(outTradeNo);
        //更新支付状态为已支付
        payLog.setTransactionId(transaction_id);
        payLog.setPayTime(new Date());
        payLog.setTradeState("1");
        payLogMapper.updateByPrimaryKeySelective(payLog);

        //更新支付日志id对应的订单的支付状态为已支付
        String[] orderIds = payLog.getOrderList().split(",");
        //update tb_order set status = '2' where order_id  in(?,?)
        TbOrder order = new TbOrder();
        order.setStatus("2");
        Example example = new Example(TbOrder.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("orderId", Arrays.asList(orderIds));

        System.out.println("orderStatus= ---------------------"+order.getStatus());
        orderMapper.updateByExampleSelective(order,example);
    }
}
