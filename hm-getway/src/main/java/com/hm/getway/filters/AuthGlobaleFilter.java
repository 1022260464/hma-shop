package com.hm.getway.filters;

import com.hm.getway.config.AuthProperties;
import com.hm.getway.utils.JwtTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;


@Component
@Order(0)
@RequiredArgsConstructor
@Slf4j
public class AuthGlobaleFilter implements GlobalFilter {

    private final AuthProperties authProperties;


    private final JwtTool jwtTool;
    //spring 提供的路径匹配器
    private  final AntPathMatcher antPathMatcher =new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();

        log.info("=== 网关收到请求：{} ===", path);

        // 1. 最优先：判断是否是白名单路径
        if (isExclude(path)) {
            log.info("✓ 白名单路径，直接放行：{}", path);
            return chain.filter(exchange);
        }

        // 2. 不是白名单，获取 token
        String token = request.getHeaders().getFirst("Authorization");

        // 3. 判断 Token 是否为空
        if (token == null || token.isEmpty()) {
            log.warn("✗ 未授权访问（无 Token）：{}", path);
            ServerHttpResponse response = exchange.getResponse();
            // 设置 HTTP 状态码为 401 未授权
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        log.debug("Token 存在，开始解析...");

        // 4. 解析 Token
        Long id;
        try {
            id = jwtTool.parseToken(token);
            log.info("✓ Token 解析成功，userId: {}", id);
        } catch (Exception e) {
            log.error("✗ Token 解析失败：{}", e.getMessage());
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        // ================== 👇 终极暴力注入区 👇 ==================

        // 5. 使用 .headers(Consumer) 直接操作底层的 HttpHeaders 对象
        ServerHttpRequest newRequest = exchange.getRequest().mutate()
                .headers(httpHeaders -> {
                    // 暴力移除可能存在的空请求头脏数据
                    httpHeaders.remove("user-info");
                    // 强制塞入真实用户 ID
                    httpHeaders.add("user-info", id.toString());
                })
                .build();

        ServerWebExchange newExchange = exchange.mutate()
                .request(newRequest)
                .build();

        log.info("=== 已成功将 user-info: {} 注入请求头并转发 ===", id);

        return chain.filter(newExchange);
    }

    private boolean isExclude(String path) {
        // 获取白名单配置
        List<String> excludePaths = authProperties.getExcludePaths();
        
        // 防止配置为空或 null
        if (excludePaths == null || excludePaths.isEmpty()) {
            log.warn("⚠️ 白名单配置为空！所有请求都将被拦截");
            return false;
        }
        
        log.debug("当前白名单路径：{}", excludePaths);
        
            // 遍历所有的白名单路径
            for (String excludePath : authProperties.getExcludePaths()) {
                // 如果有一个匹配上了
                if (antPathMatcher.match(excludePath, path)) {
                    log.debug("✓ 路径匹配成功：{} matches {}", path, excludePath);
                    // 直接终止整个方法，返回 true
                    return true;
                }
            }
            // 全都没匹配上，返回 false
            log.debug("✗ 路径匹配失败：{}", path);
            return false;
        }

}