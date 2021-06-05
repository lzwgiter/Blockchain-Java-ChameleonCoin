package com.latte.blockchain.service;

import com.latte.blockchain.entity.Transaction;
import com.latte.blockchain.entity.UserChameleonTrap;
import com.latte.blockchain.entity.Wallet;
import it.unisa.dia.gas.jpbc.Element;

import java.math.BigInteger;

/**
 * @author lzwgiter
 * @since 2021/06/02
 */
public interface IChameleonService {
    /**
     * 初始化
     */
    void setup();

    /**
     * 为用户生成变色龙哈希所需的陷门和公开参数
     *
     * @param user 用户
     */
    void setUserSecret(Wallet user);

    /**
     * 为交易生成变色龙哈希值
     *
     * @param transaction 交易信息
     * @param user        {@link Wallet} 用户
     */
    void generateHash(Transaction transaction, Wallet user);

    /**
     * 修改用户信息
     *
     * @param user {@link Wallet} 用户
     * @param hash 原先的交易哈希值
     * @return 新的交易信息
     */
    String modifyMessage(String hash, Wallet user);

    /**
     * 验证新生成的交易信息是否满足变色龙哈希特性(即哈希值不变)
     *
     * @param msg    新的交易信息
     * @param hash   原Hash值
     * @param trap   用户秘钥
     * @param usingR r'
     * @return 验证成功返回true
     */
    boolean verifyHash(Element msg, String hash, UserChameleonTrap trap, Element usingR);
}
