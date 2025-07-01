package com.tezish.demo.util;

import com.tezish.demo.services.token.TokenService;
import com.tezish.demo.services.userDetails.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Slf4j
public class AuthTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    TokenService tokenService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        if (request.getRequestURI().equals("/api/auth")
                || request.getRequestURI().equals("/api/auth/login")
        ) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = parseJwt(request);
            if (jwt != null && jwtUtil.validateJwtToken(jwt)) {
                final String email = jwtUtil.extractUsername(jwt);

                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                    // get token from redis and check if it is valid
                    Map<String, String> userTokens = tokenService.getUserTokensFromRedis(userDetails);
                    String accessToken = userTokens.get("accessToken");
                    String accessTokenExpired = userTokens.get("accessTokenExpired");
                    String accessTokenRevoked = userTokens.get("accessTokenRevoked");

                    boolean isValidToken =
                            jwt.equals(accessToken) &&
                                    "false".equals(accessTokenExpired) &&
                                    "false".equals(accessTokenRevoked);

                    if (jwtUtil.isTokenValid(jwt, userDetails) && isValidToken) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities());
                        authentication.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                        );

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    } else {
                        if (!response.isCommitted()) {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired JWT token");
                        }
                        log.error("Invalid or expired JWT token");
                        return;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
            if (!response.isCommitted()) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
            }
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }
}
