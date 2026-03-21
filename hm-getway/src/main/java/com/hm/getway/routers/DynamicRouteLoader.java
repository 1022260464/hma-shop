package com.hm.getway.routers;


import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.io.Writer;
import java.nio.file.FileAlreadyExistsException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicRouteLoader {

    public final NacosConfigManager nacosConfigManager;

    public final RouteDefinitionWriter writer;

    private final String dataId = "gateway-router.json";

    private final String group = "DEFAULT_GROUP";

    private final Set<String> routeIds = new HashSet<>();

    @PostConstruct
    public  void initRoutrConfigListener() throws NacosException {

        //项目启动先拉取一次配置。并添加配置监听器
        String configInfo = nacosConfigManager.getConfigService()
                .getConfigAndSignListener(dataId, group, 5000, new Listener() {
                    @Override
                    public Executor getExecutor() {
                        return null;
                    }

                    @Override
                    public void receiveConfigInfo(String s) {
                        //监听配置变更，需要去更新路由表
                        updateConfigInfo(s);
                        log.info("收到路由配置信息:{}", s);
                    }
                });
        //第一次读取到配置，也需要更新路由表
        updateConfigInfo(configInfo);
    }

    public void updateConfigInfo(String configInfo) {
        log.debug("监听到路由配置信息:{}", configInfo);
        //解析配置信息，转为routerDefinition对象
        List<RouteDefinition> routeDefinitions = JSONUtil.toList(configInfo, RouteDefinition.class);
        //删除旧的路由表
        for (String routeId : routeIds) {
            writer.delete(Mono.just(routeId)).subscribe();
        }
        routeIds.clear();
        //更新路由表
        routeDefinitions.forEach(routeDefinition -> {
            //更新路由表
            writer.save(Mono.just(routeDefinition)).subscribe();
            //记录路由id
            routeIds.add(routeDefinition.getId());

        });

    }
}
