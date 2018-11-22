package com.maimeng.gateway.zuul.config.dynamic;

import org.springframework.beans.BeanUtils;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wuweifeng wrote on 2018/11/21.
 */
@Component
public class PropertiesDao {

    @Resource
    private JdbcTemplate jdbcTemplate;

    private final static String SQL = "SELECT * FROM zuul_route_entity WHERE enabled = TRUE";

    public Map<String, ZuulProperties.ZuulRoute> getProperties() {
        Map<String, ZuulProperties.ZuulRoute> routes = new LinkedHashMap<>();
        List<ZuulRouteEntity> list = jdbcTemplate.query(SQL, new BeanPropertyRowMapper<>(ZuulRouteEntity.class));
        list.forEach(entity -> {
            if (StringUtils.isEmpty(entity.getPath())) {
                return;
            }
            ZuulProperties.ZuulRoute zuulRoute = new ZuulProperties.ZuulRoute();
            BeanUtils.copyProperties(entity, zuulRoute);
            routes.put(zuulRoute.getPath(), zuulRoute);
        });
        return routes;
    }
}