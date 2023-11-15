package com.cyan.springcloud.orders.service;

import com.cyan.springcloud.messagesdk.model.po.MqMessage;
import com.cyan.springcloud.model.dto.AddOrderDto;
import com.cyan.springcloud.model.dto.PayRecordDto;
import com.cyan.springcloud.model.dto.PayStatusDto;
import com.cyan.springcloud.model.po.OlPayRecord;

/**
 * 保存订单信息
 *
 * @author Cyan Chau
 * @create 2023-07-08
 */
public interface OrderService {
    /**
     * 创建商品订单
     *
     * @param addOrderDto 订单信息
     */
    PayRecordDto createOrder(String userId, AddOrderDto addOrderDto);

    /**
     * 查询支付交易记录
     *
     * @param payNo 交易记录号
     */
    OlPayRecord getPayRecordByPayno(String payNo);


    /**
     * 请求支付宝查询支付结果
     *
     * @param payNo 支付记录id
     * @return 支付记录信息
     */
    PayRecordDto queryPayResult(String payNo);

    /**
     * 保存支付宝支付结果
     *
     * @param payStatusDto 支付结果信息
     */
    void saveAliPayStatus(PayStatusDto payStatusDto);

    /**
     * 发送通知结果
     *
     * @param message
     */
    void notifyPayResult(MqMessage message);


}
