package com.latte.blockchain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 交易摘要信息
 *
 * @author float311
 * @since 2021/03/31
 */
@Data
public class TransactionDigest {
    /**
     * 交易id
     */
    private String transactionId;

    /**
     * 交易发起方地址
     */
    private String senderAddress;

    /**
     * 交易接受方地址
     */
    private String recipientAddress;

    /**
     * 交易金额
     */
    private String value;

    /**
     * 注册信息
     */
    private String registrationMsg;

    /**
     * 时间戳
     */
    private String timeStamp;

    /**
     * 储存一个交易的摘要信息
     *
     * @param transactionId    交易ID
     * @param senderAddress    交易发起方地址
     * @param recipientAddress 交易接受方地址
     * @param value            交易金额
     */
    public TransactionDigest(String transactionId, String senderAddress, String recipientAddress,
                             String registrationMsg, String value, String timeStamp) {
        this.transactionId = transactionId;
        this.senderAddress = senderAddress;
        this.recipientAddress = recipientAddress;
        this.value = value;
        this.registrationMsg = registrationMsg;
        this.timeStamp = timeStamp;
    }
}
