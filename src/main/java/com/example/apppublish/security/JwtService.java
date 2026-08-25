package com.example.apppublish.security;

import com.example.apppublish.config.AppProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtService {
  private final AppProperties properties;
  public JwtService(AppProperties properties) { this.properties = properties; }
  public String issue(Long userId, String username) { var now = Instant.now(); return Jwts.builder().subject(username).claim("uid", userId).issuedAt(Date.from(now)).expiration(Date.from(now.plus(properties.jwt().expirationMinutes(), ChronoUnit.MINUTES))).signWith(Keys.hmacShaKeyFor(properties.jwt().secret().getBytes(StandardCharsets.UTF_8))).compact(); }
  public String subject(String token) { return Jwts.parser().verifyWith(Keys.hmacShaKeyFor(properties.jwt().secret().getBytes(StandardCharsets.UTF_8))).build().parseSignedClaims(token).getPayload().getSubject(); }
}
