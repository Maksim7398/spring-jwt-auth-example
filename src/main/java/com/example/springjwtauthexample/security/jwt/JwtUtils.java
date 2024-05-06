package com.example.springjwtauthexample.security.jwt;

import com.example.springjwtauthexample.security.AppUserDetails;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Date;

@Component
@Slf4j
public class JwtUtils {

    @Value("${app.jwt.tokenExpiration}")
    private Duration tokenExpiration;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    public String generateJwtToken(AppUserDetails appUserDetails){
        return generateTokenFromUserName(appUserDetails.getUsername());
    }

    public String generateTokenFromUserName(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + tokenExpiration.toMillis()))
                .signWith(SignatureAlgorithm.HS512,jwtSecret)
                .compact();
    }

    public String getUserName(String token){
        return Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validate(String token){
        try {
            Jwts.parser()
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(token);
            return true;
        }catch (SignatureException e){
            log.error("Invalid signature: {}",e.getMessage());
        }catch (MalformedJwtException ex){
            log.error("Invalid token: {} ",ex.getMessage());
        }catch (ExpiredJwtException ex){
            log.error("Token is expired: {}",ex.getMessage());
        }catch (UnsupportedJwtException ex){
            log.error("Token is unsupported: {}",ex.getMessage());
        }catch (IllegalArgumentException ex){
            log.error("Claims string is empty: {}",ex.getMessage());
        }
        return false;
    }
}
