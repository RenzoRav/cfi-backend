package com.br.cfi.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class R2Config {

  @Value("${r2.endpoint}")      
  private String endpoint;

  @Value("${r2.bucket}")         
  private String bucket;

  @Value("${r2.access-key}")     
  private String accessKey;

  @Value("${r2.secret-key}")     
  private String secretKey;

  @Value("${r2.public-base-url:}") 
  private String publicBaseUrl;

  private StaticCredentialsProvider creds() {
    return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
  }

  @Bean
  public S3Client r2Client() {
    return S3Client.builder()
        .credentialsProvider(creds())
        .serviceConfiguration(S3Configuration.builder()
            .pathStyleAccessEnabled(true)          
            .build())
        .endpointOverride(URI.create(endpoint))   
        .region(Region.US_EAST_1)                 
        .build();
  }

  @Bean
  public S3Presigner r2Presigner() {
    return S3Presigner.builder()
        .credentialsProvider(creds())
        .serviceConfiguration(S3Configuration.builder()
            .pathStyleAccessEnabled(true)
            .build())
        .endpointOverride(URI.create(endpoint))
        .region(Region.US_EAST_1)                 
        .build();
  }

  @Bean
  public R2Props r2Props() {
    return new R2Props(endpoint, bucket, accessKey, secretKey, publicBaseUrl);
  }
}
