package com.tuguang.seckill.vo;

import com.tuguang.seckill.entity.TOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单详情返回对象
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDeatilVo {

    private TOrder tOrder;

    private GoodsVo goodsVo;
}
