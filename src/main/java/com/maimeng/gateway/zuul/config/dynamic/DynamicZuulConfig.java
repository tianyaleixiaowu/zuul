package com.maimeng.gateway.zuul.config.dynamic;

import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @author wuweifeng wrote on 2018/11/21.
 */
@Configuration
public class DynamicZuulConfig {

    @Resource
    private ZuulProperties zuulProperties;

    @Resource
    private ServerProperties serverProperties;

    @Bean
    public DynamicZuulRouteLocator routeLocator() {
        return new DynamicZuulRouteLocator(
                serverProperties.getServlet().getServletPrefix(), zuulProperties);
    }
}