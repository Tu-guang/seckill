package com.tuguang.seckill.service;

import com.tuguang.seckill.entity.TSeckillOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tuguang.seckill.entity.TUser;

/**
* @author chen
* @description 针对表【t_seckill_order(秒杀订单表)】的数据库操作Service
* @createDate 2023-07-31 11:14:49
*/
public interface TSeckillOrderService extends IService<TSeckillOrder> {
    /**
     * 获取秒杀结果
     *
     * @param tUser
     * @param goodsId
     * @return orderId 成功 ；-1 秒杀失败 ；0 排队中
     **/
    Long getResult(TUser tUser, Long goodsId);

}
