package com.vinova.booking_hotel.authentication.security;
import com.vinova.booking_hotel.common.exception.ResourceNotFoundException;
import com.vinova.booking_hotel.authentication.model.Account;
import com.vinova.booking_hotel.authentication.repository.AccountRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtUtils {

    private final UserDetailsService userDetailsService;
    private final AccountRepository accountRepository;

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${spring.app.jwtSecret}")
    private String jwtSecret;

    @Value("${spring.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    public String getJwtFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        logger.debug("Authorization Header: {}", bearerToken);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public String generateTokenFromUserDetails(UserDetails userDetails) {
        String username = userDetails.getUsername();

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(ResourceNotFoundException::new);

        // Lấy danh sách vai trò từ UserDetails
        List<String> roleNames = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // Tạo token JWT với roles

        return Jwts.builder()
                .setSubject(username)
                .claim("id", account.getId())
                .claim("email", account.getEmail())
                .claim("fullName", account.getFullName())
                .claim("phone", account.getPhone())
                .claim("avatar", account.getAvatar())
                .claim("roles", roleNames)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();
    }

    public String generateRefreshTokenFromUserDetails(UserDetails userDetails) {
        String username = userDetails.getUsername();
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .signWith(key())
                .compact();
    }

    public String refreshAccessToken(String refreshToken) {
        try {
            // Xác thực refresh token và lấy thông tin người dùng
            String username = Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parseClaimsJws(refreshToken)
                    .getBody()
                    .getSubject();

            // Lấy thông tin tài khoản từ cơ sở dữ liệu
            Account account = accountRepository.findByUsername(username)
                    .orElseThrow(ResourceNotFoundException::new);

            // Kiểm tra xem refresh token có hợp lệ?
            if (!refreshToken.equals(account.getRefreshToken()) ||
                    account.getRefreshExpiresAt() == null ||
                    account.getRefreshExpiresAt().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Invalid refresh token");
            }

            // Tạo access token mới
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            String newAccessToken = generateTokenFromUserDetails(userDetails);

            // Tạo refresh token mới
            String newRefreshToken = generateRefreshTokenFromUserDetails(userDetails);

            // Lưu refresh token mới vào cơ sở dữ liệu
            account.setRefreshToken(newRefreshToken);
            accountRepository.save(account);

            return newAccessToken;
        } catch (Exception e) {
            throw new RuntimeException("Invalid refresh token exception");
        }
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public Long getUserIdFromJwtToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("id", Long.class);
    }

    Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException | ExpiredJwtException e) {
            logger.error("Invalid or expired JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

}
