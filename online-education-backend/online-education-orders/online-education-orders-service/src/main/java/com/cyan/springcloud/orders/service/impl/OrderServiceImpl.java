package com.cyan.springcloud.orders.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyan.springcloud.base.exception.BusinessException;
import com.cyan.springcloud.base.utils.IdWorkerUtils;
import com.cyan.springcloud.base.utils.QRCodeUtil;
import com.cyan.springcloud.messagesdk.model.po.MqMessage;
import com.cyan.springcloud.messagesdk.service.MqMessageService;
import com.cyan.springcloud.model.dto.AddOrderDto;
import com.cyan.springcloud.model.dto.PayRecordDto;
import com.cyan.springcloud.model.dto.PayStatusDto;
import com.cyan.springcloud.model.po.XcOrders;
import com.cyan.springcloud.model.po.XcOrdersGoods;
import com.cyan.springcloud.model.po.XcPayRecord;
import com.cyan.springcloud.orders.config.AlipayConfig;
import com.cyan.springcloud.orders.config.PayNotifyConfig;
import com.cyan.springcloud.orders.mapper.XcOrdersGoodsMapper;
import com.cyan.springcloud.orders.mapper.XcOrdersMapper;
import com.cyan.springcloud.orders.mapper.XcPayRecordMapper;
import com.cyan.springcloud.orders.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author Cyan Chau
 * @create 2023-07-08
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Resource
    private XcOrdersMapper xcOrdersMapper;

    @Resource
    private XcOrdersGoodsMapper xcOrdersGoodsMapper;

    @Resource
    private XcPayRecordMapper xcPayRecordMapper;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private MqMessageService mqMessageService;

    @Value("${pay.qrcodeurl}")
    private String qrcodeurl;

    @Value("${pay.alipay.APP_ID}")
    private String APP_ID;

    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    private String APP_PRIVATE_KEY;

    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    private String ALIPAY_PUBLIC_KEY;

    @Override
    @Transactional
    public PayRecordDto createOrder(String userId, AddOrderDto addOrderDto) {

        //添加商品订单
        XcOrders xcOrders = saveXcOrders(userId, addOrderDto);
        //添加支付交易记录
        XcPayRecord payRecord = createPayRecord(xcOrders);
        Long payNo = payRecord.getPayNo();

        //生成二维码
        QRCodeUtil qrCodeUtil = new QRCodeUtil();
        // 支付二维码的url
        String url = String.format(qrcodeurl, payNo);
        // 二维码图片
        String qrCode = null;
        try {
            qrCode = qrCodeUtil.createQRCode(url, 200, 200);
        } catch (IOException e) {
            BusinessException.cast("生成二维码出现异常");
        }

        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(payRecord, payRecordDto);
        payRecordDto.setQrcode(qrCode);

        return payRecordDto;
    }

    @Override
    public XcPayRecord getPayRecordByPayno(String payNo) {
        return xcPayRecordMapper.selectOne(
                new LambdaQueryWrapper<XcPayRecord>()
                        .eq(XcPayRecord::getPayNo, payNo));
    }

    @Override
    public void notifyPayResult(MqMessage message) {

        // 1、消息体，转json
        String msg = JSON.toJSONString(message);
        /* 设置消息持久化 */
        Message msgObj = MessageBuilder.withBody(msg.getBytes(StandardCharsets.UTF_8))
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .build();
        // 2.全局唯一的消息ID，需要封装到CorrelationData中
        CorrelationData correlationData = new CorrelationData(message.getId().toString());
        // 3.添加callback
        correlationData.getFuture().addCallback(
                result -> {
                    if (result.isAck()) {
                        // 3.1.ack，消息成功
                        log.debug("通知支付结果消息发送成功, ID:{}", correlationData.getId());
                        //删除消息表中的记录
                        mqMessageService.completed(message.getId());
                    } else {
                        // 3.2.nack，消息失败
                        log.error("通知支付结果消息发送失败, ID:{}, 原因{}", correlationData.getId(), result.getReason());
                    }
                },
                ex -> log.error("消息发送异常, ID:{}, 原因{}", correlationData.getId(), ex.getMessage())
        );
        // 发送消息
        rabbitTemplate.convertAndSend(PayNotifyConfig.PAYNOTIFY_EXCHANGE_FANOUT, "", msgObj, correlationData);

    }

    @Override
    public void saveAliPayStatus(PayStatusDto payStatusDto) {
        // 1. 获取支付流水号
        String payNo = payStatusDto.getOut_trade_no();
        // 2. 查询数据库订单状态
        XcPayRecord payRecord = getPayRecordByPayno(payNo);
        if (payRecord == null) {
            BusinessException.cast("未找到支付记录");
        }
        XcOrders order = xcOrdersMapper.selectById(payRecord.getOrderId());
        if (order == null) {
            BusinessException.cast("找不到相关联的订单");
        }
        String statusFromDB = payRecord.getStatus();
        // 2.1 已支付，直接返回
        if ("600002".equals(statusFromDB)) {
            return;
        }
        // 3. 查询支付宝交易状态
        String tradeStatus = payStatusDto.getTrade_status();
        // 3.1 支付宝交易已成功，保存订单表和交易记录表，更新交易状态
        if ("TRADE_SUCCESS".equals(tradeStatus)) {
            // 更新支付交易表
            payRecord.setStatus("601002");
            payRecord.setOutPayNo(payStatusDto.getTrade_no());
            payRecord.setOutPayChannel("Alipay");
            payRecord.setPaySuccessTime(LocalDateTime.now());
            int updateRecord = xcPayRecordMapper.updateById(payRecord);
            if (updateRecord <= 0) {
                BusinessException.cast("更新支付交易表失败");
            }
            // 更新订单表
            order.setStatus("600002");
            int updateOrder = xcOrdersMapper.updateById(order);
            if (updateOrder <= 0) {
                log.debug("更新订单表失败");
                BusinessException.cast("更新订单表失败");
            }
        }
        // 4. 保存消息记录，参数1：支付结果类型通知；参数2：业务id；参数3：业务类型
        MqMessage mqMessage = mqMessageService.addMessage("payresult_notify", order.getOutBusinessId(), order.getOrderType(), null);
        // 5. 通知消息
        notifyPayResult(mqMessage);
    }

    @Override
    public PayRecordDto queryPayResult(String payNo) {

        XcPayRecord payRecord = getPayRecordByPayno(payNo);
        if (payRecord == null) {
            BusinessException.cast("请重新点击支付获取二维码");
        }
        // 支付状态
        String status = payRecord.getStatus();

        // 如果支付成功直接返回
        if ("601002".equals(status)) {
            PayRecordDto payRecordDto = new PayRecordDto();
            BeanUtils.copyProperties(payRecord, payRecordDto);
            return payRecordDto;
        }

        // 调用支付宝的接口查询支付宝结果
        PayStatusDto payStatusDto = queryPayResultFromAlipay(payNo);
        log.info("payStatusDto: {}", payStatusDto);
        // 获取支付结果更新支付记录表和订单表的支付状态
        saveAliPayStatus(payStatusDto);
        // 重新查询支付记录
        payRecord = getPayRecordByPayno(payNo);
        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(payRecord, payRecordDto);

        return payRecordDto;
    }

    /**
     * 请求支付宝查询支付结果
     *
     * @param payNo
     * @return
     */
    public PayStatusDto queryPayResultFromAlipay(String payNo) {
        //========请求支付宝查询支付结果=============
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.URL, APP_ID, APP_PRIVATE_KEY, "json", AlipayConfig.CHARSET, ALIPAY_PUBLIC_KEY, AlipayConfig.SIGNTYPE); //获得初始化的AlipayClient
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", payNo);
        request.setBizContent(bizContent.toString());
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
            if (!response.isSuccess()) {
                BusinessException.cast("请求支付查询查询失败");
            }
        } catch (AlipayApiException e) {
            log.error("请求支付宝查询支付结果异常:{}", e.toString(), e);
            BusinessException.cast("请求支付查询查询失败");
        }

        // 获取支付结果
        String resultJson = response.getBody();
        // 转map
        Map resultMap = JSON.parseObject(resultJson, Map.class);
        Map alipay_trade_query_response = (Map) resultMap.get("alipay_trade_query_response");
        // 支付结果
        String trade_status = (String) alipay_trade_query_response.get("trade_status");
        String total_amount = (String) alipay_trade_query_response.get("total_amount");
        String trade_no = (String) alipay_trade_query_response.get("trade_no");
        // 保存支付结果
        PayStatusDto payStatusDto = new PayStatusDto();
        payStatusDto.setOut_trade_no(payNo);
        payStatusDto.setTrade_status(trade_status);
        payStatusDto.setApp_id(APP_ID);
        payStatusDto.setTrade_no(trade_no);
        payStatusDto.setTotal_amount(total_amount);

        return payStatusDto;
    }

    /**
     * 创建商品订单
     *
     * @param userId
     * @param addOrderDto
     * @return
     */
    @Transactional
    public XcOrders saveXcOrders(String userId, AddOrderDto addOrderDto) {

        // 幂等性处理
        XcOrders order = getOrderByBusinessId(addOrderDto.getOutBusinessId());
        if (order != null) {
            return order;
        }
        order = new XcOrders();

        // 生成订单号
        long orderId = IdWorkerUtils.getInstance().nextId();
        order.setId(orderId);
        order.setTotalPrice(addOrderDto.getTotalPrice());
        order.setCreateDate(LocalDateTime.now());
        order.setStatus("600001");//未支付
        order.setUserId(userId);
        order.setOrderType(addOrderDto.getOrderType());
        order.setOrderName(addOrderDto.getOrderName());
        order.setOrderDetail(addOrderDto.getOrderDetail());
        order.setOrderDescrip(addOrderDto.getOrderDescrip());
        order.setOutBusinessId(addOrderDto.getOutBusinessId()); //选课记录id

        int insert = xcOrdersMapper.insert(order);
        if (insert <= 0) {
            BusinessException.cast("添加订单失败");
        }

        // 插入订单明细
        String orderDetailJson = addOrderDto.getOrderDetail();

        List<XcOrdersGoods> xcOrdersGoodsList = JSON.parseArray(orderDetailJson, XcOrdersGoods.class);
        xcOrdersGoodsList.forEach(goods -> {
            // 遍历插入订单明细表
            XcOrdersGoods xcOrdersGoods = new XcOrdersGoods();
            BeanUtils.copyProperties(goods, xcOrdersGoods);
            xcOrdersGoods.setOrderId(orderId);//订单号
            xcOrdersGoodsMapper.insert(xcOrdersGoods);
        });
        return order;
    }

    /**
     * 根据业务id查询订单
     *
     * @param businessId
     * @return
     */
    public XcOrders getOrderByBusinessId(String businessId) {
        XcOrders orders = xcOrdersMapper.selectOne(new LambdaQueryWrapper<XcOrders>().eq(XcOrders::getOutBusinessId, businessId));
        return orders;
    }

    /**
     * 创建支付交易记录
     *
     * @param orders
     * @return
     */
    public XcPayRecord createPayRecord(XcOrders orders) {

        // 订单id
        Long ordersId = orders.getId();
        XcOrders xcOrders = xcOrdersMapper.selectById(ordersId);

        // 如果此订单不存在，不能添加支付记录
        if (xcOrders == null) {
            BusinessException.cast("订单不存在");
        }

        // 订单状态
        String status = xcOrders.getStatus();
        // 如果此订单支付成功，不再添加支付记录，避免重复支付
        if ("601002".equals(status)) {
            BusinessException.cast("此订单已支付");
        }
        // 添加支付记录
        XcPayRecord payRecord = new XcPayRecord();

        // 支付记录号，用于传给支付宝
        payRecord.setPayNo(IdWorkerUtils.getInstance().nextId());
        payRecord.setOrderId(orders.getId()); // 商品订单号
        payRecord.setOrderName(orders.getOrderName());
        payRecord.setTotalPrice(orders.getTotalPrice());
        payRecord.setCurrency("CNY");
        payRecord.setCreateDate(LocalDateTime.now());
        payRecord.setStatus("601001"); //未支付
        payRecord.setUserId(orders.getUserId());

        int insert = xcPayRecordMapper.insert(payRecord);
        if (insert <= 0) {
            BusinessException.cast("插入支付记录失败");
        }

        return payRecord;

    }

}