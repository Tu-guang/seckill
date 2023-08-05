package com.tuguang.seckill.service;

import com.tuguang.seckill.entity.TOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tuguang.seckill.entity.TUser;
import com.tuguang.seckill.vo.GoodsVo;
import com.tuguang.seckill.vo.OrderDeatilVo;

/**
* @author chen
* @description 针对表【t_order(订单表)】的数据库操作Service
* @createDate 2023-07-31 11:14:49
*/
public interface TOrderService extends IService<TOrder> {
    /**
     * 秒杀
     *
     * @param user    用户对象
     * @param goodsVo 商品对象
     * @return com.tuguang.seckill.entity.TOrder
     **/
    TOrder secKill(TUser user, GoodsVo goodsVo);

    /**
     * 订单详情方法
     *
     * @param orderId
     * @return com.tuguang.seckill.vo.OrderDeatilVo
     **/
    OrderDeatilVo detail(Long orderId);

    /**
     * 获取秒杀地址
     *
     * @param user
     * @param goodsId
     * @return java.lang.String
     **/
    String createPath(TUser user, Long goodsId);

    /**
     * 校验秒杀地址
     *
     * @param user
     * @param goodsId
     * @param path
     * @return boolean
     **/
    boolean checkPath(TUser user, Long goodsId, String path);

    /**
     * 校验验证码
     * @param tuser
     * @param goodsId
     * @param captcha
     * @return boolean
     **/
    boolean checkCaptcha(TUser tuser, Long goodsId, String captcha);
}
