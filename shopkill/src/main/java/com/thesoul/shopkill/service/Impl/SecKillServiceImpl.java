package com.thesoul.shopkill.service.Impl;

import com.thesoul.shopkill.api.RedisLock;
import com.thesoul.shopkill.entity.SecOrder;
import com.thesoul.shopkill.entity.SecProductInfo;
import com.thesoul.shopkill.exception.SellException;
import com.thesoul.shopkill.service.SecKillService;
import com.thesoul.shopkill.service.SecOrderService;
import com.thesoul.shopkill.util.SnowflakeIdWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

@Service
@Slf4j
public class SecKillServiceImpl implements SecKillService {

    @Autowired
    private RedisLock redisLock;

    @Autowired
    private SecOrderService secOrderService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedisTemplate<String,SecOrder> redisTemplate;

    private static final int TIMEOUT = 10 * 1000;

    @Override
    public  long orderProductMockDiffUser(String productId,SecOrder secOrder) throws Exception{

        //加锁 setnx
        long orderSize = 0;
        String lock_key = "redis_lock";
        //long time = System.currentTimeMillis()+ TIMEOUT;
        String uuid = String.valueOf(SnowflakeIdWorker.generateId());
        boolean lock = redisLock.lock(lock_key+":"+productId, uuid);
        if (!lock){
            throw  new SellException("哎呦喂，人太多了");
        }
        //获得库存数量
        int stockNum = Integer.valueOf(stringRedisTemplate.opsForValue().get("stock"+productId));
        System.out.println(secOrder);
        if (stockNum <= 0 ) {
            throw new SellException("活动结束");
        } else {
            //仓库数量减一
            stringRedisTemplate.opsForValue().decrement("stock"+productId,1);
            //redis中加入订单
            redisTemplate.opsForSet().add("order"+productId,secOrder);
            orderSize = redisTemplate.opsForSet().size("order"+productId);
            if (orderSize >= 50){
                //订单信息持久化,多线程写入数据库(效率从单线程的9000s提升到了9ms)
                Set<SecOrder> members = redisTemplate.opsForSet().members("order"+productId);
                List<SecOrder> memberList = new ArrayList<>(members);
                CountDownLatch countDownLatch = new CountDownLatch(4);
                new Thread(() -> {
                    for (int i = 0; i <memberList.size() /4 ; i++) {
                        secOrderService.save(memberList.get(i));
                        countDownLatch.countDown();
                    }
                }, "therad1").start();
                new Thread(() -> {
                    for (int i = memberList.size() /4; i <memberList.size() /2 ; i++) {
                        secOrderService.save(memberList.get(i));
                        countDownLatch.countDown();
                    }
                }, "therad2").start();
                new Thread(() -> {
                    for (int i = memberList.size() /2; i <memberList.size() * 3 / 4 ; i++) {
                        secOrderService.save(memberList.get(i));
                        countDownLatch.countDown();
                    }
                }, "therad3").start();
                new Thread(() -> {
                    for (int i = memberList.size() * 3 / 4; i <memberList.size(); i++) {
                        secOrderService.save(memberList.get(i));
                        countDownLatch.countDown();
                    }
                }, "therad4").start();

                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.info("订单持久化完成");
                redisTemplate.delete("order"+productId);
            }
        }
        //解锁
        redisLock.unlock(lock_key+":"+productId,uuid);
        log.info("抢购成功 商品id=:"+ secOrder.getId());
        return orderSize;
    }
    @Override
    public SecProductInfo refreshStock(String productId) {
        return redisLock.refreshStock(productId);
    }

}
