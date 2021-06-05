package com.latte.blockchain.service.impl;

import com.latte.blockchain.entity.Transaction;
import com.latte.blockchain.entity.UserChameleonTrap;
import com.latte.blockchain.entity.Wallet;
import com.latte.blockchain.service.IChameleonService;
import com.latte.blockchain.utils.CryptoUtil;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import org.springframework.stereotype.Service;


/**
 * @author lzwgiter
 * @since 2021/06/02
 */
@Service
public class ChameleonServiceImpl implements IChameleonService {
    private Pairing pairing;

    /**
     * G1生成元
     */
    private Element P;

    private Field g1;

    private Field zr;

    @Override
    public void setup() {
        pairing = PairingFactory.getPairing("crypto/a.properties");
        this.g1 = pairing.getG1();
        this.zr = pairing.getZr();
        this.P = g1.newRandomElement().getImmutable();
    }

    @Override
    public void setUserSecret(Wallet user) {
        /*
        用户秘密x
         */
        Element x = zr.newRandomElement().getImmutable();
        user.setTrap(new UserChameleonTrap(x));
    }

    @Override
    public void generateHash(Transaction transaction, Wallet user) {
        Element r = zr.newRandomElement().getImmutable();
        Element msg = zr.newRandomElement().getImmutable();
        Element Y = P.mulZn(user.getTrap().getX()).getImmutable();

        // hash = mP + rY; Y = xP
        Element mMulP = P.mulZn(msg).getImmutable();
        Element rMulY = Y.mulZn(r).getImmutable();
        Element result = mMulP.add(rMulY);
        // k = m + rx
        Element rMulX = r.mul(user.getTrap().getX()).getImmutable();
        Element k = msg.add(rMulX).getImmutable();

        String hash = CryptoUtil.applySha256Hash(result.toBytes());
        user.getTrap().getKMap().put(hash, k);
        // 设置交易ID以及交易信息
        transaction.setId(hash);
        transaction.setRegistrationMsg(String.valueOf(msg.hashCode()));
    }

    /**
     * 对交易信息进行修改
     *
     * @param hash 原先的交易哈希值
     * @param user 用户
     * @return 新生成的消息
     */
    @Override
    public String modifyMessage(String hash, Wallet user) {
        long startTime = System.currentTimeMillis();
        // 随机选取一个r'
        Element newR = zr.newRandomElement().getImmutable();
        // 计算r'x
        Element rMulX = newR.mul(user.getTrap().getX()).getImmutable();
        // 计算得到 m' = k - r'x
        Element k = user.getTrap().getKMap().get(hash);
        Element calM = k.sub(rMulX).getImmutable();
        if (verifyHash(calM, hash, user.getTrap(), newR)) {
            return String.valueOf(calM.hashCode());
        } else {
            return null;
        }
    }

    /**
     * 验证新生成的交易信息是否满足变色龙哈希特性(即哈希值不变)
     *
     * @param msg  新的交易信息
     * @param hash 原Hash值
     * @param trap 用户秘钥
     * @return 验证成功返回true
     */
    @Override
    public boolean verifyHash(Element msg, String hash, UserChameleonTrap trap, Element usingR) {
        // 计算新hash' = m'P + r'Y = (m' + rx)P
        long startTime = System.currentTimeMillis();
        Element rMulX = usingR.mul(trap.getX()).getImmutable();
        Element mAddRMulX = msg.add(rMulX);
        Element calHash = P.mulZn(mAddRMulX);
        String newHash = CryptoUtil.applySha256Hash(calHash.toBytes());
        return newHash.equals(hash);
    }
}
