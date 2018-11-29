package com.maimeng.gateway.zuul.core.service;

import org.springframework.cloud.netflix.zuul.RoutesRefreshedEvent;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by wuweifeng on 2017/10/12.
 */
@Service
@EnableScheduling
@EnableAsync
public class RefreshRouteService {
    @Resource
    private ApplicationEventPublisher publisher;

    @Resource
    private RouteLocator routeLocator;

    public void refreshRoute() {
        RoutesRefreshedEvent routesRefreshedEvent = new RoutesRefreshedEvent(routeLocator);
        publisher.publishEvent(routesRefreshedEvent);
    }


    @Scheduled(cron = "0 */2 * * * ?")
    public void fetch() {
        refreshRoute();
    }
}
