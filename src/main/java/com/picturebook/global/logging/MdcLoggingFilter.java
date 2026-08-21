package com.picturebook.global.logging;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MdcLoggingFilter extends OncePerRequestFilter {

    private static final String TRACE_ID = "traceId";
    private static final String HEADER = "X-Request-Id";
    private static final Pattern VALID_TRACE_ID = Pattern.compile("^[A-Za-z0-9-]{8,64}$");

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String traceId = (String) request.getAttribute(TRACE_ID);
        if (traceId == null) {
            traceId = sanitize(request.getHeader(HEADER));
            request.setAttribute(TRACE_ID, traceId);
        }

        MDC.put(TRACE_ID, traceId);
        response.setHeader(HEADER, traceId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

    private String sanitize(String input) {
        if (input != null && VALID_TRACE_ID.matcher(input).matches()) {
            return input;
        }
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
