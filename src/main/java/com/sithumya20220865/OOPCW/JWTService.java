package com.sithumya20220865.OOPCW;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Date;

@Service
public class JWTService {

    private final String SECRET_KEY = Base64.getEncoder().encodeToString(
            "Osmanthus_wine_is_the_same_as_I_remember_but_where_are_those_who_share_the_memories".getBytes());
    private final long EXP_TIME = 15 * 60 * 1000; //token expires after 15 mins

    //method for generating JWT token
    public String generateToken(String user, String role) {
        return Jwts.builder()
                .setSubject(user)
                .claim("role", "ROLE_" + role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXP_TIME))
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();
    }

    //method for validating token
    public boolean validateToken(String token) {
        try {
            //validating created token
            Jwts.parser().setSigningKey(SECRET_KEY).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    //method for extracting username from token
    public String getUsername(String token) {
        Claims claims = Jwts.parser().setSigningKey(SECRET_KEY).build().parseSignedClaims(token).getBody();
        return claims.getSubject();
    }

    //method for extracting role from token
    public String getRole(String token) {
        Claims claims = Jwts.parser().setSigningKey(SECRET_KEY).build().parseSignedClaims(token).getBody();
        return claims.get("role", String.class);
    }

}
