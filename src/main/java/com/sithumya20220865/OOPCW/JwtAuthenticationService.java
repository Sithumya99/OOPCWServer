package com.sithumya20220865.OOPCW;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Service
public class JwtAuthenticationService {
    private final JWTService jwtService;

    public JwtAuthenticationService(JWTService jwtService) {this.jwtService = jwtService;}

    public Authentication authenticate(HttpServletRequest request) {
        //get authorization header
        String authorizationHeader = request.getHeader("Authorization");
        System.out.println("authenticate auth header: " + authorizationHeader);

        //get token from header
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            System.out.println("Authorization token: " + token);

            //validate token
            if (jwtService.validateToken(token)) {
                //extract user details
                String user = jwtService.getUsername(token);
                System.out.println("Authorization user: " + user);

                //get roles
                List<SimpleGrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority(jwtService.getRole(token)));
                System.out.println("Authorization user roles: " + authorities);

                //new authentication object
                return new UsernamePasswordAuthenticationToken(user, token, authorities);
            }
        }
        return null;
    }
}
