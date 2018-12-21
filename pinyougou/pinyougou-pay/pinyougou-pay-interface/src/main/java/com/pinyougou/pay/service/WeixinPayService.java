package com.pinyougou.pay.service;

import java.util.Map;

public interface WeixinPayService {
    /**
     * 获取支付二维码链接、总金额、操作结果、交易号
     * @param outTradeNo 交易号
     * @param totalFee 本次要支付的总金额
     * @return 支付二维码链接、总金额、操作结果、交易号
     */
    Map<String, String> createNative(String outTradeNo, String totalFee) throws Exception;

    /**
     * 根据支付日志id查询支付状态
     * @param outTradeNo 支付日志id
     * @return 操作结果
     */
    Map<String, String> queryPayStatus(String outTradeNo);
}
