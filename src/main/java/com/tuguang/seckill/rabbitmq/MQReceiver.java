package com.tuguang.seckill.rabbitmq;

import com.tuguang.seckill.entity.TSeckillOrder;
import com.tuguang.seckill.entity.TUser;
import com.tuguang.seckill.service.TGoodsService;
import com.tuguang.seckill.service.TOrderService;
import com.tuguang.seckill.utils.JsonUtil;
import com.tuguang.seckill.vo.GoodsVo;
import com.tuguang.seckill.vo.SeckillMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 消息消费者
 *
 * @ClassName: MQReceiver
 */
@Service
@Slf4j
public class MQReceiver {

    @Autowired
    private TGoodsService tGoodsService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TOrderService tOrderService;


    /**
     * 下单操作
     *
     * @param
     * @return void
     **/
    @RabbitListener(queues = "seckillQueue")
    public void receive(String message) {
        log.info("接收消息：" + message);
        SeckillMessage seckillMessage = JsonUtil.jsonStr2Object(message, SeckillMessage.class);
        Long goodsId = seckillMessage.getGoodsId();
        TUser user = seckillMessage.getTUser();
        GoodsVo goodsVo = tGoodsService.findGoodsVobyGoodsId(goodsId);
        if (goodsVo.getStockCount() < 1) {
            return;
        }
        //判断是否重复抢购
        TSeckillOrder tSeckillOrder = (TSeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
        if (tSeckillOrder != null) {
            return;
        }
        //下单操作
        tOrderService.secKill(user, goodsVo);

    }


//    @RabbitListener(queues = "queue")
//    public void receive(Object msg) {
//        System.out.println("接收到的消息" + msg);
//    }
//
//
//    @RabbitListener(queues = "queue_fanout01")
//    public void receive01(Object msg) {
//        log.info("QUEUE01接收消息" + msg);
//    }
//
//    @RabbitListener(queues = "queue_fanout02")
//    public void receive02(Object msg) {
//        log.info("QUEUE02接收消息" + msg);
//    }
//
//    @RabbitListener(queues = "queue_direct01")
//    public void receive03(Object msg) {
//        log.info("QUEUE01接收消息" + msg);
//    }
//
//    @RabbitListener(queues = "queue_direct02")
//    public void receive04(Object msg) {
//        log.info("QUEUE02接收消息" + msg);
//    }
//
//    @RabbitListener(queues = "queue_topic01")
//    public void receive05(Object msg) {
//        log.info("QUEUE01接收消息" + msg);
//    }
//
//    @RabbitListener(queues = "queue_topic02")
//    public void receive06(Object msg) {
//        log.info("QUEUE02接收消息" + msg);
//    }
//
//    @RabbitListener(queues = "queue_header01")
//    public void receive07(Message message) {
//        log.info("QUEUE01接收消息 message对象" + message);
//        log.info("QUEUE01接收消息" + new String(message.getBody()));
//    }
//
//    @RabbitListener(queues = "queue_header02")
//    public void receive08(Message message) {
//        log.info("QUEUE02接收消息 message对象" + message);
//        log.info("QUEUE02接收消息" + new String(message.getBody()));
//    }

}
