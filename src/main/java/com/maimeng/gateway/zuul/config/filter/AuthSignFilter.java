package com.maimeng.gateway.zuul.config.filter;

import com.maimeng.gateway.zuul.config.jwt.JwtUtils;
import com.maimeng.gateway.zuul.core.service.PtUserService;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.xiaoleilu.hutool.date.DateUtil;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static com.maimeng.gateway.zuul.config.Constant.USER_ID;
import static com.xiaoleilu.hutool.date.DatePattern.NORM_DATETIME_MINUTE_PATTERN;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

/**
 * @author wuweifeng wrote on 2018/11/22.
 */
@Component
public class AuthSignFilter extends ZuulFilter {
    @Resource
    private JwtUtils jwtUtils;
    @Value("${gate.ignore.startWith}")
    private String startWith;
    @Value("${gate.ignore.contain}")
    private String contain;
    @Resource
    private PtUserService ptUserService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest serverHttpRequest = ctx.getRequest();
        //类似于 /youplus/company/error
        String requestPath = serverHttpRequest.getRequestURI();
        // 不进行拦截的地址
        if (isStartWith(requestPath) || isContains(requestPath)) {
            return null;
        }

        String jwtToken = serverHttpRequest.getHeader(AUTHORIZATION);
        if (jwtToken == null) {
            //没有Authorization
            print("{\"code\":401}");
            return null;
        }
        Claims claims = jwtUtils.getClaimByToken(jwtToken);
        if (claims == null) {
            print("{\"code\":401}");
            return null;
        }
        logger.info("token的过期时间是：" + DateUtil.format(claims.getExpiration(), NORM_DATETIME_MINUTE_PATTERN));
        if (jwtUtils.isTokenExpired(claims.getExpiration())) {
            print("{\"code\":401}");
            return null;
        }

        //获取用户ID，开始校验用户的菜单权限
        String userId = claims.getSubject();

        String method = serverHttpRequest.getMethod().toUpperCase();
        //if (!ptUserService.checkMenu(Long.valueOf(userId), requestPath, method)) {
            //没有Authorization    TODO
            //return noAuth(exchange);
        //}
        ctx.addZuulRequestHeader(USER_ID, userId);

        return null;
    }

    private void print(String s) {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletResponse response = ctx.getResponse();
        response.setContentType("text/html; charset=utf-8");
        ctx.setSendZuulResponse(false);
        ctx.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
        try {
            PrintWriter out = response.getWriter();
            out.print(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 是否包含某种特征
     */
    private boolean isContains(String requestUri) {
        for (String s : contain.split(",")) {
            if (requestUri.contains(s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * URI是否以什么打头
     */
    private boolean isStartWith(String requestUri) {
        for (String s : startWith.split(",")) {
            if (requestUri.startsWith(s)) {
                return true;
            }
        }
        return false;
    }
}
