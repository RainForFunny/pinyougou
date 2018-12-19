package com.pinyougou.order.service;

import com.pinyougou.pojo.TbOrder;
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
}