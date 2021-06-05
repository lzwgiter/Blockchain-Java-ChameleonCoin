# ChameleonCoin
ChameleonCoin是一个使用了变色龙哈希技术的、单节点多用户、模拟区块链系统。
该系统由LatteCoin修改而来，LatteCoin系统提供了一个较为完善的区块链模拟环境，你可以在其中研究、验证、考量你自己的密码学方案，该项目就是一个例子。

## 变色龙哈希技术
该技术一方面用于对交易哈希进行计算，另一方面也提供了在保证交易哈希值不变的条件下，使用用户陷门对交易数据进行修改，实现区块数据可编辑的特性。

## 国密SM2
该系统中使用国密算法SM2进行用户的公私钥生成、交易签名等，相较于ECC，SM2国密算法提供了同等或更好的效率以及安全性。


## 模拟区块链
该系统初始化会自动生成指定数量的用户，支持进行交易。

# 用法
所需：
    - 保证后台数据库(MYSQL)启动，并创建用户chain_admin、数据库lattechain。
    - 若要修改代码并重建，请添加[JPBC](http://gas.dia.unisa.it/projects/jpbc/download.html#.YLsOl4XitEY)依赖

启动：
`java -jar chamelelonCoin.jar`