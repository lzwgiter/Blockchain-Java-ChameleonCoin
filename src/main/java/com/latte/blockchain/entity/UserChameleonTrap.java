package com.latte.blockchain.entity;

import it.unisa.dia.gas.jpbc.Element;
import lombok.Data;

import java.util.HashMap;

/**
 * @author lzwgiter
 * @since 2021/06/02
 */
@Data
public class UserChameleonTrap {
    Element x;

    HashMap<String, Element> kMap;

    public UserChameleonTrap(Element x) {
        this.x = x;
        this.kMap = new HashMap<>();
    }
}
