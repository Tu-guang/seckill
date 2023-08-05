package com.tuguang.seckill.mapper;

import com.tuguang.seckill.entity.TGoods;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tuguang.seckill.vo.GoodsVo;

import java.util.List;


/**
* @author chen
* @description 针对表【t_goods(商品表)】的数据库操作Mapper
* @createDate 2023-07-31 11:14:49
* @Entity com.tuguang.seckill.entity.TGoods
*/
public interface TGoodsMapper extends BaseMapper<TGoods> {
    /**
     * 返回商品列表
     * @param
     * @return java.util.List<com.tuguang.seckill.vo.GoodsVo>
     **/
    List<GoodsVo> findGoodsVo();

    GoodsVo findGoodsVobyGoodsId(Long goodsId);
}




