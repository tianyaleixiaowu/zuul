package com.maimeng.gateway.zuul.config.exception;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.util.ZuulRuntimeException;
import org.springframework.util.ReflectionUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.ERROR_TYPE;

/**
 * @author wuweifeng wrote on 2018/11/27.
 */
//@Component
public class ErrorFilter extends ZuulFilter {

    private static Logger log = LoggerFactory.getLogger(ErrorFilter.class);
    protected static final String SEND_ERROR_FILTER_RAN = "sendErrorFilter.ran";

    @Override
    public String filterType() {
        //异常过滤器
        return ERROR_TYPE;
    }

    @Override
    public int filterOrder() {
        //优先级，数字越大，优先级越低
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        //是否执行该过滤器，true代表需要过滤
        RequestContext ctx = RequestContext.getCurrentContext();
        // only forward to errorPath if it hasn't been forwarded to already
        //return ctx.getThrowable() != null
        //        && !ctx.getBoolean(SEND_ERROR_FILTER_RAN, false);
        return true;
    }

    @Override
    public Object run() {
        try {
            RequestContext ctx = RequestContext.getCurrentContext();
            ZuulException exception = findZuulException(ctx.getThrowable());
            HttpServletRequest request = ctx.getRequest();

            request.setAttribute("javax.servlet.error.status_code", exception.nStatusCode);

            log.warn("Error during filtering", exception);
            request.setAttribute("javax.servlet.error.exception", exception);

            print(exception.nStatusCode, exception.getMessage());
            //if (StringUtils.hasText(exception.getMessage())) {
            //    request.setAttribute("javax.servlet.error.message", exception.getMessage());
            //}

            //RequestDispatcher dispatcher = request.getRequestDispatcher(
            //        "/error");
            //if (dispatcher != null) {
            //    ctx.set(SEND_ERROR_FILTER_RAN, true);
            //    if (!ctx.getResponse().isCommitted()) {
            //        ctx.setResponseStatusCode(exception.nStatusCode);
            //        dispatcher.forward(request, ctx.getResponse());
            //    }
            //}
        } catch (Exception ex) {
            ReflectionUtils.rethrowRuntimeException(ex);
        }

        return null;
    }

    private void print(int code, String s) {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletResponse response = ctx.getResponse();
        response.setContentType("text/html; charset=utf-8");
        ctx.setSendZuulResponse(false);
        ctx.setResponseStatusCode(code);
        try {
            PrintWriter out = response.getWriter();
            out.print(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    ZuulException findZuulException(Throwable throwable) {
        if (throwable.getCause() instanceof ZuulRuntimeException) {
            // this was a failure initiated by one of the local filters
            return (ZuulException) throwable.getCause().getCause();
        }

        if (throwable.getCause() instanceof ZuulException) {
            // wrapped zuul exception
            return (ZuulException) throwable.getCause();
        }

        if (throwable.getCause() instanceof NoLoginException) {
            // exception thrown by zuul lifecycle
            return (ZuulException) throwable;
        }

        if (throwable instanceof ZuulException) {
            // exception thrown by zuul lifecycle
            return (ZuulException) throwable;
        }

        // fallback, should never get here
        return new ZuulException(throwable, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null);
    }

}