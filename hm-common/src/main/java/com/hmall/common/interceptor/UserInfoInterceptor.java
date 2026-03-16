package com.hmall.common.interceptor;

import cn.hutool.core.util.StrUtil;
import com.hmall.common.utils.UserContext;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;

public class UserInfoInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        System.out.println("🕵️ 拦截器执行啦！路径：" + request.getRequestURI() + "，方法：" + request.getMethod());

        // ================== 👇 打印所有到达 Tomcat 的 Header 👇 ==================
        System.out.println("====== 下游微服务收到的所有 Header ======");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            System.out.println(name + " : " + request.getHeader(name));
        }
        System.out.println("=========================================");

        String userInfo = request.getHeader("user-info");
        System.out.println("最终提取到的 user-info 的值是：" + userInfo);

        if (StrUtil.isNotBlank(userInfo)) {
            UserContext.setUser(Long.valueOf(userInfo));
        }
        return true;
    }
}