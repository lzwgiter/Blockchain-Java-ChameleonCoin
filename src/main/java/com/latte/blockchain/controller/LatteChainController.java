package com.latte.blockchain.controller;

import com.latte.blockchain.entity.Transaction;
import com.latte.blockchain.service.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;


/**
 * @author float311
 * @since 2021/01/31
 */
@Controller
public class LatteChainController {

    @Autowired
    private IUserService userService;

    @Autowired
    private IChainService chainService;

    @Autowired
    private ITransactionService transactionService;

    /**
     * 初始化LatteChain区块链系统，初始化预置账户并创建创世块
     */
    @GetMapping("/init")
    public String initSystem(Model model) {
        if (chainService.initChain()) {
            return "init";
        } else {
            model.addAttribute("msg", "初始化失败，已初始化或系统错误！");
            return "error";
        }
    }

    /**
     * 查看当前所有的账户信息
     */
    @GetMapping("/allUsersInfo")
    public String getAllUsersInfo(Model model) {
        model.addAttribute("usersInfo", userService.getAllUsersInfo());
        return "allUsers";
    }

    /**
     * 交易发起接口
     *
     * @param sender    发起方账户地址
     * @param recipient 接受方账户地址
     * @param value     交易金额
     * @return String
     */
    @PostMapping(path = "/trade")
    public String sendFunds(@RequestParam(name = "sender") String sender,
                            @RequestParam(name = "recipient") String recipient,
                            @RequestParam(name = "value") float value,
                            Model model) {
        model.addAttribute("transactionInfo",
                transactionService.createTransaction(sender, recipient, value));
        return "transaction";
    }

    /**
     * @param id 交易ID
     * @return 交易信息
     */
    @PostMapping(path = "/getT")
    @ResponseBody
    public String getTransaction(@RequestParam(name = "transactionId") String id) {
        return transactionService.getTransaction(id);
    }

    @PostMapping(path = "/modify")
    @ResponseBody
    public void modifyTransaction(@RequestParam(name = "transactionId") String id,
                                  @RequestParam(name = "userName") String name) {
        transactionService.modifyTransaction(name, id);
    }
}
