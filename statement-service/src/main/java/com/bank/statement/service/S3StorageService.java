package com.bank.statement.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3StorageService {

    private final S3Client s3Client;
    private final String bucketName;

    public S3StorageService(S3Client s3Client,
                            @Value("${aws.s3.bucket-name:bank-statements}") String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    public void upload(String key, byte[] content) {
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        s3Client.putObject(putRequest, RequestBody.fromBytes(content));
    }

    public byte[] download(String key) {
        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        return s3Client.getObjectAsBytes(getRequest).asByteArray();
    }
}
