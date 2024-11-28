package com.sithumya20220865.OOPCW.Services;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JwtAuthenticationService {
    private final JWTService jwtService;

    public JwtAuthenticationService(JWTService jwtService) {this.jwtService = jwtService;}

    public Authentication authenticate(HttpServletRequest request) {
        //get authorization header
        String authorizationHeader = request.getHeader("Authorization");

        //get token from header
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);

            //validate token
            if (jwtService.validateToken(token)) {
                //extract user details
                String user = jwtService.getUsername(token);

                //get roles
                List<SimpleGrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority(jwtService.getRole(token)));

                //new authentication object
                return new UsernamePasswordAuthenticationToken(user, token, authorities);
            }
        }
        return null;
    }
}
