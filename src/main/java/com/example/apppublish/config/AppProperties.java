package com.example.apppublish.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Jwt jwt, String bootstrapAdminPassword, Storage storage, Apk apk, String publicBaseUrl) {
  public record Jwt(String secret, long expirationMinutes) {}
  public record Storage(String endpoint, String accessKey, String secretKey, String bucket) {}
  public record Apk(String aapt2Path, String apksignerPath) {}
}
