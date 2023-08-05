package com.tuguang.seckill.config;


import com.tuguang.seckill.entity.TUser;

/**
 * @ClassName: UserContext
 */
public class UserContext {

    private static ThreadLocal<TUser> userThreadLocal = new ThreadLocal<>();

    public static void setUser(TUser tUser) {
        userThreadLocal.set(tUser);
    }

    public static TUser getUser() {
        return userThreadLocal.get();
    }
}
