package com.tuguang.seckill.service;

import com.tuguang.seckill.entity.TUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tuguang.seckill.vo.LoginVo;
import com.tuguang.seckill.vo.RespBean;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
* @author chen
* @description 针对表【t_user(用户表)】的数据库操作Service
* @createDate 2023-07-31 11:11:12
*/
public interface TUserService extends IService<TUser> {

    /**
     * 登录方法
     *
     * @param loginVo
     * @param request
     * @param response
     * @return com.tuguang.seckill.vo.RespBean
     **/
    RespBean doLongin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response);

    /**
     * 根据cookie获取用户
     *
     * @param userTicket
     * @return com.tuguang.seckill.entity.TUser
     **/
    TUser getUserByCookie(String userTicket, HttpServletRequest request, HttpServletResponse response);


    /**
     * 更新密码
     *
     * @param userTicket
     * @param password
     * @return com.tuguang.seckill.vo.RespBean
     **/
    RespBean updatePassword(String userTicket, String password, HttpServletRequest request, HttpServletResponse response);
}
