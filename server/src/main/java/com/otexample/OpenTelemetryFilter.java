package com.otexample;

import com.otexample.metrics.EventManagerImpl;
import io.grpc.Context;
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.context.Scope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
public class OpenTelemetryFilter implements Filter {

    @Autowired
    private OpenTelemetryConfigurer otProxy;


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        Context c = OpenTelemetry.getPropagators().getTextMapPropagator().extract(Context.current(), ((HttpServletRequest) servletRequest), HttpServletRequest::getHeader);

        try (Scope ss = EventManagerImpl.getInstance().startEventWithParent("myevent", "apo", "123", c)) {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    @Override
    public void destroy() {

    }
}
