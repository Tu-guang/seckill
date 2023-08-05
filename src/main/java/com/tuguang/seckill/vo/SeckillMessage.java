package com.tuguang.seckill.vo;

import com.tuguang.seckill.entity.TUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 秒杀信息
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeckillMessage {

    private TUser tUser;

    private Long goodsId;
}
