package com.tuguang.seckill.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tuguang.seckill.config.AccessLimit;
import com.tuguang.seckill.entity.TOrder;
import com.tuguang.seckill.entity.TSeckillOrder;
import com.tuguang.seckill.entity.TUser;
import com.tuguang.seckill.exception.GlobalException;
import com.tuguang.seckill.rabbitmq.MQSender;
import com.tuguang.seckill.service.TGoodsService;
import com.tuguang.seckill.service.TOrderService;
import com.tuguang.seckill.service.TSeckillOrderService;
import com.tuguang.seckill.utils.JsonUtil;
import com.tuguang.seckill.vo.GoodsVo;
import com.tuguang.seckill.vo.RespBean;
import com.tuguang.seckill.vo.RespBeanEnum;
import com.tuguang.seckill.vo.SeckillMessage;
import com.wf.captcha.ArithmeticCaptcha;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀
 *
 * @ClassName: SeKillController
 */
@Slf4j
@Controller
@RequestMapping("/seckill")
@Api(value = "秒杀", tags = "秒杀")
public class SeKillController implements InitializingBean {

    @Autowired
    private TGoodsService tGoodsService;
    @Autowired
    private TSeckillOrderService tSeckillOrderService;
    @Autowired
    private TOrderService torderService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MQSender mqSender;
    @Autowired
    private RedisScript<Long> redisScript;

    private Map<Long, Boolean> EmptyStockMap = new HashMap<>();


    @ApiOperation("获取验证码")
    @GetMapping(value = "/captcha")
    public void verifyCode(TUser tUser, Long goodsId, HttpServletResponse response) {
        if (tUser == null || goodsId < 0) {
            throw new GlobalException(RespBeanEnum.REQUEST_ILLEGAL);
        }
        //设置请求头为输出图片的类型
        response.setContentType("image/jpg");
        response.setHeader("Pargam", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        //生成验证码
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(130, 32, 3);
        redisTemplate.opsForValue().set("captcha:" + tUser.getId() + ":" + goodsId, captcha.text(), 300, TimeUnit.SECONDS);
        try {
            captcha.out(response.getOutputStream());
        } catch (IOException e) {
            log.error("验证码生成失败", e.getMessage());
        }
    }

    /**
     * 获取秒杀地址
     *
     * @param tuser
     * @param goodsId
     * @param captcha
     * @return com.tuguang.seckill.vo.RespBean
     **/
    @ApiOperation("获取秒杀地址")
    @AccessLimit(second = 5, maxCount = 5, needLogin = true)
    @GetMapping(value = "/path")
    @ResponseBody
    public RespBean getPath(TUser tuser, Long goodsId, String captcha, HttpServletRequest request) {
        if (tuser == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
//        ValueOperations valueOperations = redisTemplate.opsForValue();
        //限制访问次数，5秒内访问5次
//        String uri = request.getRequestURI();
//        captcha = "0";
//        Integer count = (Integer) valueOperations.get(uri + ":" + tuser.getId());
//        if (count == null) {
//            valueOperations.set(uri + ":" + tuser.getId(), 1, 5, TimeUnit.SECONDS);
//        } else if (count < 5) {
//            valueOperations.increment(uri + ":" + tuser.getId());
//        } else {
//            return RespBean.error(RespBeanEnum.ACCESS_LIMIT_REACHED);
//        }


        boolean check = torderService.checkCaptcha(tuser, goodsId, captcha);
        if (!check) {
            return RespBean.error(RespBeanEnum.ERROR_CAPTCHA);
        }
        String str = torderService.createPath(tuser, goodsId);
        return RespBean.success(str);
    }


    /**
     * 获取秒杀结果
     *
     * @param tUser
     * @param goodsId
     * @return orderId 成功 ；-1 秒杀失败 ；0 排队中
     **/
    @ApiOperation("获取秒杀结果")
    @GetMapping("getResult")
    @ResponseBody
    public RespBean getResult(TUser tUser, Long goodsId) {
        if (tUser == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        Long orderId = tSeckillOrderService.getResult(tUser, goodsId);
        return RespBean.success(orderId);
    }

    /**
     * 秒杀功能
     *
     * @param user
     * @param goodsId
     * @return java.lang.String
     **/
    @ApiOperation("秒杀功能")
    @RequestMapping(value = "/{path}/doSeckill", method = RequestMethod.POST)
    @ResponseBody
    public RespBean doSecKill(@PathVariable String path, TUser user, Long goodsId) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        //优化后代码
        ValueOperations valueOperations = redisTemplate.opsForValue();
        boolean check = torderService.checkPath(user, goodsId, path);
        if (!check) {
            return RespBean.error(RespBeanEnum.REQUEST_ILLEGAL);
        }

        //判断是否重复抢购
        TSeckillOrder tSeckillOrder = (TSeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
        if (tSeckillOrder != null) {
            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
        }
        //内存标记，减少Redis的访问
        if (EmptyStockMap.get(goodsId)) {
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        //预减库存
//        Long stock = valueOperations.decrement("seckillGoods:" + goodsId);
        Long stock = (Long) redisTemplate.execute(redisScript, Collections.singletonList("seckillGoods:" + goodsId), Collections.EMPTY_LIST);
        if (stock < 0) {
            EmptyStockMap.put(goodsId, true);
            valueOperations.increment("seckillGoods:" + goodsId);
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        SeckillMessage seckillMessag = new SeckillMessage(user, goodsId);

        /***
         * - 使用消息队列处理异步
         * - 订单生成在消息监控部分
         */
        mqSender.sendSeckillMessage(JsonUtil.object2JsonStr(seckillMessag));
        return RespBean.success(0);


        /*
//        model.addAttribute("user", user);
        GoodsVo goodsVo = itGoodsService.findGoodsVobyGoodsId(goodsId);
        if (goodsVo.getStockCount() < 1) {
//            model.addAttribute("errmsg", RespBeanEnum.EMPTY_STOCK.getMessage());
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        //判断是否重复抢购
//        TSeckillOrder seckillOrder = itSeckillOrderService.getOne(new QueryWrapper<TSeckillOrder>().eq("user_id", user.getId()).eq("goods_id", goodsId));
        TSeckillOrder seckillOrder = (TSeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsVo.getId());
        if (seckillOrder != null) {
//          model.addAttribute("errmsg", RespBeanEnum.REPEATE_ERROR.getMessage());
            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
        }
        TOrder tOrder = orderService.secKill(user, goodsVo);
//        model.addAttribute("order", tOrder);
//        model.addAttribute("goods", goodsVo);
        return RespBean.success(tOrder);

         */

    }

    /**
     * 秒杀功能---没有加入秒杀地址、验证码
     *
     * @param user
     * @param goodsId
     * @return java.lang.String
     **/
    @ApiOperation("秒杀功能")
    @RequestMapping(value = "/doSeckill", method = RequestMethod.POST)
    @ResponseBody
    public RespBean doSecKill_temp(TUser user, Long goodsId) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        //优化后代码
        ValueOperations valueOperations = redisTemplate.opsForValue();
//        boolean check = torderService.checkPath(user, goodsId, path);
//        if (!check) {
//            return RespBean.error(RespBeanEnum.REQUEST_ILLEGAL);
//        }

        //判断是否重复抢购
        TSeckillOrder tSeckillOrder = (TSeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
        if (tSeckillOrder != null) {
            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
        }
        //内存标记，减少Redis的访问
        if (EmptyStockMap.get(goodsId)) {
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        //预减库存
//        Long stock = valueOperations.decrement("seckillGoods:" + goodsId);
        Long stock = (Long) redisTemplate.execute(redisScript, Collections.singletonList("seckillGoods:" + goodsId), Collections.EMPTY_LIST);
        if (stock < 0) {
            EmptyStockMap.put(goodsId, true);
            valueOperations.increment("seckillGoods:" + goodsId);
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        SeckillMessage seckillMessag = new SeckillMessage(user, goodsId);
        mqSender.sendSeckillMessage(JsonUtil.object2JsonStr(seckillMessag));
        return RespBean.success(0);
    }

    /**
     * 秒杀功能-最简单的秒杀功能-废弃
     *
     * @param model
     * @param user
     * @param goodsId
     * @return java.lang.String
     **/
    @ApiOperation("秒杀功能-废弃")
    @RequestMapping(value = "/doSeckill2", method = RequestMethod.POST)
    public String doSecKill2(Model model, TUser user, Long goodsId) {
        model.addAttribute("user", user);
        GoodsVo goodsVo = tGoodsService.findGoodsVobyGoodsId(goodsId);
        if (goodsVo.getStockCount() < 1) {
            model.addAttribute("errmsg", RespBeanEnum.EMPTY_STOCK.getMessage());
            return "secKillFail";
        }
        //判断是否重复抢购
        TSeckillOrder seckillOrder = tSeckillOrderService.getOne(new QueryWrapper<TSeckillOrder>().eq("user_id", user.getId()).eq("goods_id", goodsId));
        if (seckillOrder != null) {
            model.addAttribute("errmsg", RespBeanEnum.REPEATE_ERROR.getMessage());
            return "secKillFail";
        }
        TOrder tOrder = torderService.secKill(user, goodsVo);
        model.addAttribute("order", tOrder);
        model.addAttribute("goods", goodsVo);
        return "orderDetail";
    }

    /**
     * 系统初始化，把商品库存数量加载到Redis
     *
     * @param
     * @return void
     **/
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> list = tGoodsService.findGoodsVo();
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        list.forEach(goodsVo -> {
            redisTemplate.opsForValue().set("seckillGoods:" + goodsVo.getId(), goodsVo.getStockCount());
            EmptyStockMap.put(goodsVo.getId(), false);
        });
    }
}
