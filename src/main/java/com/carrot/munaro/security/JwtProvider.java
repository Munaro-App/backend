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

    private final long refreshTokenExpiredTime =
            1000L * 60 * 60 * 24 * 14;

    public String createToken(Long userId) {
        return createAccessToken(userId);
    }

    public String createAccessToken(Long userId) {
        return createToken(userId, accessTokenExpiredTime, "access");
    }

    public String createRefreshToken(Long userId) {
        return createToken(userId, refreshTokenExpiredTime, "refresh");
    }

    private String createToken(
            Long userId,
            long expiredTime,
            String tokenType
    ) {

        Date now = new Date();

        Date expiredDate =
                new Date(now.getTime() + expiredTime);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("type", tokenType)
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
