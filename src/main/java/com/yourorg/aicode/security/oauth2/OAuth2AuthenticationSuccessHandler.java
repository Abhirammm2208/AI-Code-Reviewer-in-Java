package com.yourorg.aicode.security.oauth2;

import com.yourorg.aicode.security.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Value("${app.oauth2.redirectUri:http://localhost:3000/oauth2/callback}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        try {
            String email = null;
            Object principal = authentication.getPrincipal();
            if (principal instanceof OAuth2User oAuth2User) {
                Object emailAttr = oAuth2User.getAttributes().get("email");
                if (emailAttr != null) {
                    email = String.valueOf(emailAttr);
                }
            }
            if (email == null || email.isBlank()) {
                // Fallback to authentication name (may not be an email for some providers)
                email = authentication.getName();
            }

            String token = tokenProvider.generateTokenFromEmail(email);
            String targetUrl = buildTargetUrl(token);
            response.sendRedirect(targetUrl);
        } catch (Exception ex) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "OAuth2 success handling failed");
        }
    }

    private String buildTargetUrl(String token) {
        String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
        if (redirectUri.contains("?")) {
            return redirectUri + "&token=" + encodedToken;
        }
        return redirectUri + "?token=" + encodedToken;
    }
}
