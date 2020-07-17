package com.thesoul.shopkill.service.Impl;

import com.thesoul.shopkill.api.RedisLock;
import com.thesoul.shopkill.entity.SecOrder;
import com.thesoul.shopkill.entity.SecProductInfo;
import com.thesoul.shopkill.exception.SellException;
import com.thesoul.shopkill.service.SecMQKillService;
import com.thesoul.shopkill.util.SnowflakeIdWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SecMQKillServiceImpl implements SecMQKillService {

    @Autowired
    private RedisLock redisLock;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final int TIMEOUT = 10 * 1000;

    @Override
    public  long orderProductMockDiffUser(String productId,SecOrder secOrder) throws Exception{

        //加锁 setnx
        long orderSize = 0;
        String lock_key = "redis_lock";
        String uuid = String.valueOf(SnowflakeIdWorker.generateId());
        boolean lock = redisLock.lock(lock_key+":"+productId, uuid);
        if (!lock){
            throw  new SellException("哎呦喂，人太多了");
        }
        //获得库存数量
        int stockNum = Integer.valueOf(stringRedisTemplate.opsForValue().get("stock"+productId));
        if (stockNum <= 0 ) {
            throw new SellException("活动结束");
        } else {
            //仓库数量减一
            stringRedisTemplate.opsForValue().decrement("stock"+productId,1);
        }
        //解锁
        redisLock.unlock(lock_key+":"+productId,uuid);
        return orderSize;
    }
    @Override
    public SecProductInfo refreshStock(String productId) {
        return redisLock.refreshStock(productId);
    }

}
