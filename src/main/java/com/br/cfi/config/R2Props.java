package com.br.cfi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "r2")
public record R2Props(
    String endpoint,       // ex: https://2af9...r2.cloudflarestorage.com
    String bucket,         // ex: cfi-media
    String accessKey,      // Access Key ID
    String secretKey,      // Secret Access Key
    String publicBaseUrl   // opcional: domínio público (CDN). Pode ser null
) {}
