package com.cyan.springcloud.model.dto;

import com.cyan.springcloud.model.po.XcPayRecord;
import lombok.Data;
import lombok.ToString;

/**
 * 支付交易记录信息及二维码信息
 */
@Data
@ToString
public class PayRecordDto extends XcPayRecord {

    // 二维码
    private String qrcode;

}
