package com.latte.blockchain.service.impl;

import com.latte.blockchain.repository.TransactionRepo;
import com.latte.blockchain.repository.TransactionPoolRepo;
import com.latte.blockchain.repository.UtxoRepo;
import com.latte.blockchain.entity.*;
import com.latte.blockchain.service.IChameleonService;
import com.latte.blockchain.service.ITransactionService;
import com.latte.blockchain.service.IWalletService;
import com.latte.blockchain.utils.CryptoUtil;

import com.latte.blockchain.utils.JsonUtil;
import com.latte.blockchain.utils.LatteChain;
import com.latte.blockchain.utils.LockUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashSet;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author float311
 * @since 2021/01/29
 */
@Service
@Slf4j
public class TransactionServiceImpl implements ITransactionService {

    private final LatteChain latteChain = LatteChain.getInstance();

    @Autowired
    private IWalletService walletService;

    @Autowired
    private IChameleonService chameleonService;

    /**
     * 交易DAO对象
     */
    @Autowired
    private TransactionRepo transactionRepo;

    /**
     * 交易池DAO对象
     */
    @Autowired
    private TransactionPoolRepo transactionPoolRepo;

    /**
     * UTXO DAO对象
     */
    @Autowired
    private UtxoRepo utxoRepo;

    /**
     * 发起一笔交易
     *
     * @param sender    交易发起方
     * @param recipient 交易接受方
     * @param value     交易金额
     * @return String 交易信息
     */
    @Override
    public Transaction createTransaction(String sender, String recipient, float value) {
        // 处理网络原因导致的字符问题
        sender = sender.replace(" ", "+");
        recipient = recipient.replace(" ", "+");
        Transaction newTransaction = walletService.sendFunds(sender, recipient, value);
        ReentrantLock requestLock = LockUtil.getLockUtil().getStateLock();
        Condition condition = LockUtil.getLockUtil().getWriteCondition();
        // 若交易建立成功，则将交易放入交易池
        if (newTransaction != null) {
            requestLock.lock();
            try {
                transactionRepo.save(newTransaction);
                transactionPoolRepo.save(
                        new TransactionsPoolEntity(newTransaction.getId(), newTransaction.getTimeStamp()));
                log.info("新交易已提交！id: " + newTransaction.getId());
                condition.signalAll();
            } finally {
                requestLock.unlock();
            }
            return newTransaction;
        } else {
            return null;
        }
    }

    /**
     * 计算交易输出
     *
     * @param transaction {@link Transaction} 交易
     * @return 交易成功则返回true
     */
    @Override
    public boolean processTransaction(Transaction transaction) {
        // 首先检查一个交易的合法性
        // 验签
        if (!isValidSignature(transaction)) {
            log.warn("交易" + transaction.getId() + "签名信息异常！请审计该交易！");
            return false;
        }
        float inputsValue = getInputsValue(transaction);
        // 计算剩余价值
        if (inputsValue == 0) {
            // 输入已经被消耗
            return false;
        }

        // 从全局删除交易方的UTXO
        for (String inputId : transaction.getInputUtxosId()) {
            utxoRepo.deleteById(inputId);
        }

        float leftOver = inputsValue - transaction.getValue();

        PublicKey senderAddress = latteChain.getUsers().get(transaction.getSenderString()).getPublicKey();
        PublicKey recipientAddress = latteChain.getUsers().get(transaction.getRecipientString()).getPublicKey();

        // 添加交易输出
        transaction.setOutputUtxos(new HashSet<>());
        Utxo sendUtxo = new Utxo(recipientAddress, transaction.getValue());
        Utxo backUtxo = new Utxo(senderAddress, leftOver);
        transaction.getOutputUtxosId().add(sendUtxo.getId());
        transaction.getOutputUtxosId().add(backUtxo.getId());

        sendUtxo.setRefTransactionId(transaction.getId());
        backUtxo.setRefTransactionId(transaction.getId());
        // 将金额发送至接收方
        transaction.getOutputUtxos().add(sendUtxo);
        // 将剩余金额返回至发送方
        transaction.getOutputUtxos().add(backUtxo);
        transactionRepo.saveAndFlush(transaction);
        return true;
    }

    /**
     * 获取交易输入的总值
     *
     * @param transaction 待计算交易
     * @return 输入总值
     */
    @Override
    public float getInputsValue(Transaction transaction) {
        float total = 0;
        Utxo output;
        for (String input : transaction.getInputUtxosId()) {
            output = utxoRepo.getTransactionOutputById(input);
            if (output == null) {
                return 0;
            } else {
                total += output.getValue();
            }
        }
        return total;
    }

    @Override
    public void generateSignature(PrivateKey privateKey, Transaction transaction) {
        transaction.setSignature(CryptoUtil.applySm2Signature(privateKey, transaction.getData()));
    }

    /**
     * 验证交易签名
     *
     * @param transaction {@link Transaction} 交易
     * @return 是否为一个合法的签名信息
     */
    @Override
    public boolean isValidSignature(Transaction transaction) {
        Wallet senderWallet = latteChain.getUsers().get(transaction.getSenderString());
        transaction.setSender(senderWallet.getPublicKey());
        return CryptoUtil.verifySm2Signature(transaction.getSender(),
                transaction.getData(),
                transaction.getSignature());
    }

    @Override
    public String getTransaction(String id) {
        if (!transactionRepo.existsById(id)) {
            // 查询交易不存在
            return null;
        } else {
            long startTime = System.currentTimeMillis();
            Transaction transaction = transactionRepo.getTransactionById(id);
            TransactionDigest digest = new TransactionDigest(id, transaction.getSenderString(), transaction.getRecipientString(),
                    transaction.getValue().toString(), transaction.getRegistrationMsg(), Long.toString(transaction.getTimeStamp()));
            long endTime = System.currentTimeMillis();
            System.out.println(Thread.currentThread().getName() + "消耗时间:" + (endTime - startTime) + "ms");
            return JsonUtil.toJson(digest);
        }
    }

    @Override
    public void modifyTransaction(String sender, String id) {
        Wallet senderUser = latteChain.getUsers().get(sender);
        long startTime = System.currentTimeMillis();
        Transaction transaction = transactionRepo.getTransactionById(id);
        String newMsg = chameleonService.modifyMessage(transaction.getId(), senderUser);
        if (newMsg != null) {
            transaction.setRegistrationMsg(newMsg);
            transactionRepo.save(transaction);
            System.out.println("修改交易信息耗时：" + (System.currentTimeMillis() - startTime) + "ms");
        } else {
            System.out.println("修改失败");
        }
    }
}