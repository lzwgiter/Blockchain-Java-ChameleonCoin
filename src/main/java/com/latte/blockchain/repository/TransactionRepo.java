package com.latte.blockchain.repository;

import com.latte.blockchain.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 交易DAO对象访问类
 *
 * @author float311
 * @since 2021/02/21
 */
public interface TransactionRepo extends JpaRepository<Transaction, String> {

    /**
     * 获取指定id的交易类
     *
     * @param id 索引
     * @return Transaction
     */
    Transaction getTransactionById(String id);
}
