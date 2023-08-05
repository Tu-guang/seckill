package com.tuguang.seckill.controller;


import com.tuguang.seckill.service.TUserService;
import com.tuguang.seckill.vo.LoginVo;
import com.tuguang.seckill.vo.RespBean;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
@Slf4j
@RequestMapping("/login")
@Api(value = "登录", tags = "登录")
public class UserController {

    @Autowired
    private TUserService tUserService;


    /**
     * @param
     * @return
     */
    @ApiOperation("跳转登录页面")
    @RequestMapping(value = "/toLogin", method = RequestMethod.GET)
    public String toLogin() {
        return "login";
    }

    @ApiOperation("登录接口")
    @RequestMapping(value = "/doLogin", method = RequestMethod.POST)
    @ResponseBody
    public RespBean doLogin(@Valid LoginVo loginVo, HttpServletRequest request, HttpServletResponse response) {
        log.info("{}", loginVo);
        return tUserService.doLongin(loginVo, request, response);
    }

}
