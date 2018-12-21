package com.pinyougou.order.service;

import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.service.BaseService;
import com.pinyougou.vo.PageResult;

public interface OrderService extends BaseService<TbOrder> {

    PageResult search(Integer page, Integer rows, TbOrder order);

    /**
     * 添加订单，返回支付日志id
     * @param order
     * @return
     */
    String addOrder(TbOrder order);

    /**
     * 根据支付日志id获取支付日志对象
     * @param outTradeNo
     * @return
     */
    TbPayLog findPayLogById(String outTradeNo);

    /**
     * 根据交易号（支付日志id）和微信支付订单号更新订单状态
     * @param outTradeNo 交易号（支付日志id）
     * @param transaction_id 微信支付订单号
     */
    void updateOrderStatus(String outTradeNo, String transaction_id);
}