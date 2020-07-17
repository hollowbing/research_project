package com.thesoul.shopkill.api;


import com.thesoul.shopkill.entity.ProductInfo;
import com.thesoul.shopkill.entity.SecProductInfo;
import com.thesoul.shopkill.exception.SellException;
import com.thesoul.shopkill.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * 分布式乐观锁
 */
@Component
@Slf4j
public class RedisLock {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ProductService productService;



    /*
    加锁
     */
    public boolean lock(String key,String value){
        //System.out.println("加锁");
        //setIfAbsent对应redis中的setnx，key存在的话返回false，不存在返回true

        if ( redisTemplate.opsForValue().setIfAbsent(key,value,2, TimeUnit.SECONDS)){
            return true;
        }
        //两个问题，Q1超时时间
       /* String currentValue = redisTemplate.opsForValue().get(key);
        if (!StringUtils.isEmpty(currentValue) && Long.parseLong(currentValue) < System.currentTimeMillis()){
            //Q2 在线程超时的时候，多个线程争抢锁的问题
            String oldValue = redisTemplate.opsForValue().getAndSet(key, value);
            if (!StringUtils.isEmpty(oldValue) && oldValue.equals(currentValue)){
                return true;
            }
        }*/
        return false;
    }

    public void unlock(String key ,String value){
        try{
            String currentValue = redisTemplate.opsForValue().get(key);
            if (!StringUtils.isEmpty(currentValue) && currentValue.equals(value)){
                redisTemplate.opsForValue().getOperations().delete(key);
            }
        }catch(Exception e){
            log.error("redis分布上锁解锁异常, {}",e);
        }

        // 释放锁资源 (保证获取值和删除操作的原子性) LUA脚本保证删除的原子性

        /*String[] key1 = null;
        key1[0] = key;
        String[] value1 = null;
        value1[0] = value;
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        this.redisTemplate.execute(new DefaultRedisScript<>(script),
                Arrays.asList(key1), Arrays.asList(value1));*/


    }

    public SecProductInfo refreshStock(String productId){
        SecProductInfo secProductInfo = new SecProductInfo();
        ProductInfo productInfo = productService.findOne(productId);
        if (productId == null){
            //throw new SellException("秒杀商品不存在");
            System.out.println("秒杀商品不存在");
        }
        try{
            redisTemplate.opsForValue().set("stock"+productInfo.getProductId(),String.valueOf(productInfo.getProductStock()));
            String value = redisTemplate.opsForValue().get("stock"+productInfo.getProductId());
            secProductInfo.setProductId(productId);
            secProductInfo.setStock(value);
        }catch(Exception e){
            log.error(e.getMessage());
        }
        return secProductInfo;

    }


}
