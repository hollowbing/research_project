package com.thesoul.shopkill.service;

import com.thesoul.shopkill.config.RabbitConfig;
import com.thesoul.shopkill.dao.SecOrderMapper;
import com.thesoul.shopkill.entity.SecOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MQOrderService {
    @Autowired
    private SecOrderMapper secOrderMapper;
    /**
     * 监听订单消息队列，并消费
     *
     * @param
     */
    @RabbitListener(queues = RabbitConfig.ORDER_QUEUE)
    public void createOrder(SecOrder secOrder) {
        log.info("收到订单入库消息：{}", secOrder.getId());
        /**
         * 调用数据库secOrderMapper订单入库
         */
        secOrderMapper.save(secOrder);
    }

}
