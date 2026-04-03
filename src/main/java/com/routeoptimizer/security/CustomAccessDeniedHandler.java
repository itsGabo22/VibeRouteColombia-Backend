package com.routeoptimizer.security;
 
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
 
import java.io.IOException;
import java.io.PrintWriter;
 
/**
 * Custom handler to ensure 403 Forbidden errors are returned as JSON.
 * This resolves the professor's feedback about inconsistent error types.
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
 
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
 
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
 
        PrintWriter writer = response.getWriter();
        writer.println("{ \"status\": 403, \"error\": \"Forbidden\", \"message\": \"You do not have permission to access this resource. Role higher than current is required.\", \"path\": \"" + request.getRequestURI() + "\" }");
    }
}
