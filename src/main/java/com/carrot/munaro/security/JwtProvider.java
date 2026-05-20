package com.carrot.munaro.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtProvider {

    private final String secretKey =
            "your-secret-key-your-secret-key-your-secret-key";

    private final long accessTokenExpiredTime =
            1000L * 60 * 60;

    public String createToken(Long userId) {

        Date now = new Date();

        Date expiredDate =
                new Date(now.getTime() + accessTokenExpiredTime);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(now)
                .setExpiration(expiredDate)
                .signWith(
                        Keys.hmacShaKeyFor(secretKey.getBytes()),
                        SignatureAlgorithm.HS256
                )
                .compact();
    }

    public Long getUserId(String token) {

        Claims claims =
                Jwts.parserBuilder()
                        .setSigningKey(
                                Keys.hmacShaKeyFor(secretKey.getBytes())
                        )
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

        return Long.parseLong(claims.getSubject());
    }

    public boolean validateToken(String token) {

        try {

            Jwts.parserBuilder()
                    .setSigningKey(
                            Keys.hmacShaKeyFor(secretKey.getBytes())
                    )
                    .build()
                    .parseClaimsJws(token);

            return true;

        } catch (Exception e) {
            return false;
        }
    }
}