package com.otexample;

import com.atlassian.obsvs.event.EventManagerImpl;
import com.atlassian.obsvs.event.ObsvsEventCallerMetadata;
import com.atlassian.obsvs.event.ObsvsEventInput;
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.context.Context;
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


        try (Scope ignored = EventManagerImpl.getInstance().startEvent(ObsvsEventInput.builder()
                .spanBuilder(OpenTelemetry.getTracer("example").spanBuilder("serverEvent"))
                .eventCallerMetadata(ObsvsEventCallerMetadata.builder()
                        .tenantId("tt")
                        .eventName("server-event")
                        .build()).build())) {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    @Override
    public void destroy() {

    }
}
