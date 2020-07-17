package com.thesoul.shopkill.util;

import com.thesoul.shopkill.entity.ProductInfo;
import com.thesoul.shopkill.entity.SecOrder;

import java.math.RoundingMode;
import java.util.Random;

public class SecUtils {

    /*
    创建虚拟订单
     */
    public  static SecOrder createDummyOrder(ProductInfo productInfo){
        String key = String.valueOf(SnowflakeIdWorker.generateId());
        SecOrder secOrder = new SecOrder();
        secOrder.setId(key);
        secOrder.setUserId("userId="+key);
        secOrder.setProductId(productInfo.getProductId());
        secOrder.setProductPrice(productInfo.getProductPrice().setScale(2, RoundingMode.HALF_UP));
        secOrder.setAmount(productInfo.getProductPrice().setScale(2,RoundingMode.HALF_UP));
        return secOrder;
    }

    /*
    伪支付(可以扩展)
     */
/*
    public static  boolean dummyPay(){
        Random random = new Random();
        int result = random.nextInt(1000) % 2;
        if (result == 0){
            return true;
        }
        return false;
    }
*/

}
