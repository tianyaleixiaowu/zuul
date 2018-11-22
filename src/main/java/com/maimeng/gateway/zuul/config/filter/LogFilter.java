package com.maimeng.gateway.zuul.config.filter;

import com.maimeng.gateway.zuul.config.Constant;
import com.maimeng.gateway.zuul.config.filter.body.BodyReaderHttpServletRequestWrapper;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.netflix.zuul.http.HttpServletRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static com.maimeng.gateway.zuul.config.Constant.USER_ID;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

/**
 * @author wuweifeng wrote on 2018/11/21.
 */
@Component
public class LogFilter extends ZuulFilter {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest serverHttpRequest = ctx.getRequest();
        String method = serverHttpRequest.getMethod();
        logger.info("-------------------用户发起请求-----------------");
        // 记录下请求内容
        logger.info("URL : " + serverHttpRequest.getRequestURL().toString());
        logger.info("HTTP_METHOD : " + method);
        logger.info("Content-type：" + serverHttpRequest.getHeader(CONTENT_TYPE));
        logger.info("userId为：" + serverHttpRequest.getHeader(USER_ID));
        logger.info("传参为：");

        if (Constant.APP_JSON.equals(serverHttpRequest.getHeader(Constant.CONTENT_TYPE))) {
            //记录application/json时的传参，SpringMVC中使用@RequestBody接收的值
            HttpServletRequestWrapper httpServletRequestWrapper = (HttpServletRequestWrapper) serverHttpRequest;
            BodyReaderHttpServletRequestWrapper wrapper = (BodyReaderHttpServletRequestWrapper)
                    httpServletRequestWrapper.getRequest();
            String s = wrapper.getBody();
            logger.info(s);
        } else {
            //记录请求的键值对
            for (String key : serverHttpRequest.getParameterMap().keySet()) {
                logger.info(key + "->" + serverHttpRequest.getParameter(key));
            }
        }
        return null;
    }

    private void print(String s) {
        HttpServletResponse response = RequestContext.getCurrentContext().getResponse();
        response.setContentType("text/html; charset=utf-8");
        try {
            PrintWriter out = response.getWriter();
            out.print(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
