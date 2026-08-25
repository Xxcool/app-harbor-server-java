package com.example.apppublish.service;

import com.example.apppublish.config.AppProperties;
import io.minio.*;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import java.io.InputStream;

@Service
public class ObjectStorageService {
  private final MinioClient client; private final AppProperties properties;
  public ObjectStorageService(AppProperties properties) { this.properties = properties; this.client = MinioClient.builder().endpoint(properties.storage().endpoint()).credentials(properties.storage().accessKey(), properties.storage().secretKey()).build(); }
  @PostConstruct public void ensureBucket() throws Exception { var bucket = properties.storage().bucket(); if (!client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build()); }
  public void put(String key, InputStream stream, long size, String contentType) throws Exception { client.putObject(PutObjectArgs.builder().bucket(properties.storage().bucket()).object(key).stream(stream, size, -1).contentType(contentType).build()); }
  public InputStream get(String key) throws Exception { return client.getObject(GetObjectArgs.builder().bucket(properties.storage().bucket()).object(key).build()); }
  public void remove(String key) throws Exception { client.removeObject(RemoveObjectArgs.builder().bucket(properties.storage().bucket()).object(key).build()); }
}
