package com.shopflow.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.*;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GatewayJwtFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final Set<String> TRUSTED_HEADERS = Set.of("x-user-id", "x-user-email", "x-user-role");
    private static final List<String> PUBLIC_PATH_PREFIXES = List.of(
            "/api/auth/",
            "/actuator/health",
            "/actuator/info"
    );

    private final SecretKey signingKey;

    public GatewayJwtFilter(@Value("${jwt.secret}") String base64Secret) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // Always strip spoofed X-User-* headers even on public endpoints
        if (isPublicPath(path)) {
            filterChain.doFilter(new IdentityHeaderRequestWrapper(request, Map.of()), response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            writeUnauthorized(response, "Missing or invalid Authorization header", path);
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Map<String, String> identityHeaders = Map.of(
                    "X-User-Id", String.valueOf(claims.get("userId", Long.class)),
                    "X-User-Email", claims.getSubject(),
                    "X-User-Role", claims.get("role", String.class)
            );

            filterChain.doFilter(new IdentityHeaderRequestWrapper(request, identityHeaders), response);
        } catch (JwtException | IllegalArgumentException ex) {
            writeUnauthorized(response, "Invalid or expired JWT token", path);
        }
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATH_PREFIXES.stream().anyMatch(path::startsWith);
    }

    private void writeUnauthorized(HttpServletResponse response, String message, String path) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("""
                {"status":401,"error":"Unauthorized","message":"%s","path":"%s"}
                """.formatted(message, path));
    }

    /**
     * Wraps the incoming Servlet request to remove any client-spoofed X-User-* headers
     * and inject verified identity headers extracted from the signed JWT.
     */
    private static class IdentityHeaderRequestWrapper extends HttpServletRequestWrapper {

        private final Map<String, String> injectedHeaders;

        IdentityHeaderRequestWrapper(HttpServletRequest request, Map<String, String> injectedHeaders) {
            super(request);
            this.injectedHeaders = injectedHeaders;
        }

        @Override
        public String getHeader(String name) {
            for (Map.Entry<String, String> entry : injectedHeaders.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(name)) {
                    return entry.getValue();
                }
            }
            if (TRUSTED_HEADERS.contains(name.toLowerCase())) {
                return null;
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            for (Map.Entry<String, String> entry : injectedHeaders.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(name)) {
                    return Collections.enumeration(List.of(entry.getValue()));
                }
            }
            if (TRUSTED_HEADERS.contains(name.toLowerCase())) {
                return Collections.emptyEnumeration();
            }
            return super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            Set<String> names = new LinkedHashSet<>();
            Enumeration<String> original = super.getHeaderNames();
            while (original.hasMoreElements()) {
                String headerName = original.nextElement();
                if (!TRUSTED_HEADERS.contains(headerName.toLowerCase())) {
                    names.add(headerName);
                }
            }
            names.addAll(injectedHeaders.keySet());
            return Collections.enumeration(names);
        }
    }
}