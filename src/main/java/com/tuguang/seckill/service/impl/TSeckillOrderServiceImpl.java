package com.tuguang.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tuguang.seckill.entity.TSeckillOrder;
import com.tuguang.seckill.entity.TUser;
import com.tuguang.seckill.service.TSeckillOrderService;
import com.tuguang.seckill.mapper.TSeckillOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
* @author chen
* @description 针对表【t_seckill_order(秒杀订单表)】的数据库操作Service实现
* @createDate 2023-07-31 11:14:49
*/
@Service
public class TSeckillOrderServiceImpl extends ServiceImpl<TSeckillOrderMapper, TSeckillOrder>
    implements TSeckillOrderService{

    @Autowired
    private TSeckillOrderMapper tSeckillOrderMapper;
    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public Long getResult(TUser tUser, Long goodsId) {

        TSeckillOrder tSeckillOrder = tSeckillOrderMapper.selectOne(new QueryWrapper<TSeckillOrder>().eq("user_id", tUser.getId()).eq("goods_id", goodsId));
        if (null != tSeckillOrder) {
            return tSeckillOrder.getOrderId();
        } else if (redisTemplate.hasKey("isStockEmpty:" + goodsId)) {
            return -1L;
        } else {
            return 0L;
        }

    }

}




