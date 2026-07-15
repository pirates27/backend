package com.landlens.api.interceptor;

import com.landlens.api.service.DeveloperApiService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ApiKeyInterceptor implements HandlerInterceptor {

    @Autowired
    private DeveloperApiService developerApiService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Only intercept external APIs
        String uri = request.getRequestURI();
        if (!uri.startsWith("/api/v1/external")) {
            return true;
        }

        String apiKey = request.getHeader("x-api-key");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing API Key (x-api-key header)");
            return false;
        }

        request.setAttribute("api_start_time", System.currentTimeMillis());
        request.setAttribute("api_raw_key", apiKey);

        int[] statusCodeOut = new int[1];
        com.landlens.api.model.ApiKey keyObj = developerApiService.validateApiKey(apiKey, statusCodeOut);

        if (keyObj == null) {
            response.setStatus(statusCodeOut[0]);
            if (statusCodeOut[0] == 429) {
                response.getWriter().write("Rate limit exceeded for this API key");
            } else {
                response.getWriter().write("Invalid or expired API Key");
            }
            // For logging failed attempts as well, we can write a mock entry (without associated ApiKey object in DB or skip)
            return false;
        }

        request.setAttribute("api_key_obj", keyObj);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        String uri = request.getRequestURI();
        if (!uri.startsWith("/api/v1/external")) {
            return;
        }

        Long startTime = (Long) request.getAttribute("api_start_time");
        com.landlens.api.model.ApiKey keyObj = (com.landlens.api.model.ApiKey) request.getAttribute("api_key_obj");

        if (startTime != null && keyObj != null) {
            long duration = System.currentTimeMillis() - startTime;
            developerApiService.logRequest(
                    keyObj,
                    uri,
                    request.getMethod(),
                    response.getStatus(),
                    request.getRemoteAddr(),
                    duration
            );
        }
    }
}
