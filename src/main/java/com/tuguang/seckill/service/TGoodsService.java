package com.tuguang.seckill.service;

import com.tuguang.seckill.entity.TGoods;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tuguang.seckill.vo.GoodsVo;

import java.util.List;

/**
* @author chen
* @description 针对表【t_goods(商品表)】的数据库操作Service
* @createDate 2023-07-31 11:14:49
*/
public interface TGoodsService extends IService<TGoods> {
    /**
     * 返回商品列表
     *
     * @param
     * @return java.util.List<com.tuguang.seckill.vo.GoodsVo>
     **/
    List<GoodsVo> findGoodsVo();

    /**
     * 获取商品详情
     *
     * @param goodsId
     * @return java.lang.String
     **/
    GoodsVo findGoodsVobyGoodsId(Long goodsId);
}
