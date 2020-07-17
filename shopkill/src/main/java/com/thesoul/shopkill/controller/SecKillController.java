package com.thesoul.shopkill.controller;

import com.thesoul.shopkill.config.RabbitConfig;
import com.thesoul.shopkill.entity.ProductInfo;
import com.thesoul.shopkill.entity.SecOrder;
import com.thesoul.shopkill.entity.SecProductInfo;
import com.thesoul.shopkill.service.SecMQKillService;
import com.thesoul.shopkill.util.SecUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@Slf4j
@RequestMapping("/skill")
public class SecKillController {

    @Autowired
    private SecMQKillService secMQKillService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Resource
    private RedisTemplate<String,SecOrder> redisTemplate;

    /*
    下单，同时将订单信息保存在redis中，随后将数据持久化
     */
    @GetMapping("/order/{productId}")
    public String skill(@PathVariable String productId) throws Exception{
        //判断是否抢光
        int amount = Integer.valueOf(stringRedisTemplate.opsForValue().get("stock"+productId));
        if (amount <= 0){
            System.out.println("活动结束了。");
            return "不好意思，活动结束啦";
        }
        //初始化抢购商品信息，创建虚拟订单。
        ProductInfo productInfo = new ProductInfo(productId);
        SecOrder secOrder = SecUtils.createDummyOrder(productInfo);

        //付款，付款时时校验库存，如果成功redis存储订单信息，库存加1
        try {
            secMQKillService.orderProductMockDiffUser(productId,secOrder);
        }catch (Exception e){
            return e.getMessage();
        }
        //入消息队列
        rabbitTemplate.convertAndSend(RabbitConfig.ORDER_EXCHANGE, RabbitConfig.ORDER_ROUTING_KEY, secOrder);

        return "订单数量: "+redisTemplate.opsForSet().size("order"+productId)+
                "  剩余数量:"+Integer.valueOf(stringRedisTemplate.opsForValue().get("stock"+productId));
    }
    /*
     在redis中刷新库存
     */
    @GetMapping("/refresh/{productId}")
    public String  refreshStock(@PathVariable String productId) throws Exception{
        SecProductInfo secProductInfo = secMQKillService.refreshStock(productId);
        return "库存id为 "+productId +" <br>  库存总量为 "+secProductInfo.getStock();
    }

}