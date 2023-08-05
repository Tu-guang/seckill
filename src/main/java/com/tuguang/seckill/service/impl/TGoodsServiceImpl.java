package com.tuguang.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tuguang.seckill.entity.TGoods;
import com.tuguang.seckill.service.TGoodsService;
import com.tuguang.seckill.mapper.TGoodsMapper;
import com.tuguang.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author chen
* @description 针对表【t_goods(商品表)】的数据库操作Service实现
* @createDate 2023-07-31 11:14:49
*/
@Service
public class TGoodsServiceImpl extends ServiceImpl<TGoodsMapper, TGoods>
    implements TGoodsService{

    @Autowired
    private TGoodsMapper tGoodsMapper;

    @Override
    public List<GoodsVo> findGoodsVo() {
        return tGoodsMapper.findGoodsVo();
    }

    @Override
    public GoodsVo findGoodsVobyGoodsId(Long goodsId) {
        return tGoodsMapper.findGoodsVobyGoodsId(goodsId);
    }
}




