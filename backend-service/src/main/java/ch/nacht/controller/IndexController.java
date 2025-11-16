package ch.nacht.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class IndexController {

    @Value("${spring.security.oauth2.client.provider.external.issuer-uri}")
    private String issuerUri;

    @GetMapping(path = "/")
    public HashMap index() {
        // get a successful user login
        OAuth2User user = ((OAuth2User)SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        return new HashMap(){{
            put("hello", user.getAttribute("name"));
            put("your email is", user.getAttribute("email"));
        }};
    }


    @GetMapping(path = "/unauthenticated")
    public HashMap unauthenticatedRequests() {
        return new HashMap(){{
            put("this is ", "unauthenticated endpoint");
        }};
    }

    @PostMapping(path = "/api/logout")
    public Map<String, String> logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }

        // Construct Keycloak logout URL
        String keycloakLogoutUrl = issuerUri + "/protocol/openid-connect/logout?redirect_uri=" +
                                    request.getScheme() + "://" + request.getServerName() + ":" +
                                    request.getServerPort() + "/unauthenticated";

        Map<String, String> logoutResponse = new HashMap<>();
        logoutResponse.put("message", "Logged out successfully");
        logoutResponse.put("keycloakLogoutUrl", keycloakLogoutUrl);

        return logoutResponse;
    }
}
