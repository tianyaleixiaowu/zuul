package com.maimeng.gateway.zuul.config.exception;

import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.cloud.netflix.zuul.util.ZuulRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author wuweifeng wrote on 2018/11/27.
 */
@RestController
public class ErrorHandlerController implements ErrorController {

    @RequestMapping(value = "/error")
    public ResponseEntity<ErrorBean> error(HttpServletRequest request) {
        RequestContext ctx = RequestContext.getCurrentContext();
        ZuulException exception = findZuulException(ctx.getThrowable());

        ErrorBean errorBean = new ErrorBean();
        errorBean.setCode(exception.nStatusCode);
        errorBean.setMessage(exception.getMessage());
        //errorBean.setReason("程序出错");
        return new ResponseEntity<>(errorBean, HttpStatus.BAD_GATEWAY);
    }

    @Override
    public String getErrorPath() {
        return "/error";
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

    private static class ErrorBean {
        private int code;

        private String message;

        private String reason;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}